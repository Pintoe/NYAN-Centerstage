package org.firstinspires.ftc.teamcode.utilities.robot;

import android.annotation.SuppressLint;

import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.PoseVelocity2d;
import com.acmerobotics.roadrunner.Time;
import com.acmerobotics.roadrunner.Twist2dDual;
import com.acmerobotics.roadrunner.Vector2d;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.teamcode.utilities.localizer.ThreeWheelLocalizer;
import org.firstinspires.ftc.teamcode.utilities.robot.subsystems.DepositLift;
import org.firstinspires.ftc.teamcode.utilities.robot.subsystems.Drivetrain;
import org.firstinspires.ftc.teamcode.utilities.robot.subsystems.Intake;
import org.firstinspires.ftc.teamcode.utilities.robot.subsystems.InternalIMU;
import org.firstinspires.ftc.teamcode.utilities.robot.subsystems.ClimbLift;
import org.firstinspires.ftc.teamcode.utilities.robot.subsystems.PlaneLauncher;
import org.firstinspires.ftc.teamcode.utilities.robot.subsystems.Subsystem;
import org.mercurialftc.mercurialftc.silversurfer.encoderticksconverter.EncoderTicksConverter;
import org.mercurialftc.mercurialftc.silversurfer.encoderticksconverter.Units;
import org.mercurialftc.mercurialftc.silversurfer.geometry.Pose2D;
import org.mercurialftc.mercurialftc.silversurfer.geometry.Vector2D;
import org.mercurialftc.mercurialftc.silversurfer.tracker.ThreeWheelTracker;
import org.mercurialftc.mercurialftc.silversurfer.tracker.WheeledTrackerConstants;
import org.mercurialftc.mercurialftc.util.hardware.Encoder;


import java.util.List;

public class RobotEx {
    private static RobotEx robotInstance = null;

    List<LynxModule> allHubs;

    public InternalIMU internalIMU = InternalIMU.getInstance();

    public Drivetrain drivetrain = new Drivetrain();
    public ClimbLift climbLift = new ClimbLift();
    public DepositLift depositLift = new DepositLift();
    public Intake intake = new Intake();
    public VoltageSensor voltageSensor;

    public PlaneLauncher planeLauncher = new PlaneLauncher();
    private final ElapsedTime frameTimer = new ElapsedTime();

    private final Subsystem[] robotSubsystems = new Subsystem[]{
            drivetrain,
            climbLift,
            intake,
            depositLift,
            planeLauncher
    };

    Telemetry telemetry;

    public ThreeWheelLocalizer localizer;

    private double voltageCompensator = 12;
    public Pose2d pose = new Pose2d(0, 0, 0);

    private RobotEx() {
        if (RobotEx.robotInstance != null) {
            throw new IllegalStateException("Robot already instantiated");
        }
    }

    public static RobotEx getInstance() {
        if (RobotEx.robotInstance == null) {
            RobotEx.robotInstance = new RobotEx();
        }

        return RobotEx.robotInstance;
    }

    public void init(HardwareMap hardwareMap, Telemetry telemetry) {


        this.allHubs = hardwareMap.getAll(LynxModule.class);
        this.voltageSensor = hardwareMap.voltageSensor.iterator().next();
        this.voltageCompensator = this.voltageSensor.getVoltage();


        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        for (Subsystem subsystem : this.robotSubsystems) {
            subsystem.onInit(hardwareMap, telemetry);
        }

        internalIMU.onInit(hardwareMap, telemetry);

        this.localizer = new ThreeWheelLocalizer(
                new BaseEncoder(this.drivetrain.leftBackMotor, -1), // 0
                new BaseEncoder(this.drivetrain.rightBackMotor, 1), // 3
                new BaseEncoder(this.drivetrain.leftFrontMotor, -1), // 2
                internalIMU,
                telemetry
        );


        telemetry.update();

        this.telemetry = telemetry;
    }

    public void postInit() {
        for (Subsystem subsystem : this.robotSubsystems) {
            subsystem.onOpmodeStarted();
        }

        internalIMU.onOpmodeStarted();
    }

    @SuppressLint("")
    public double update() {

        for (LynxModule hub : allHubs) {
            hub.clearBulkCache();
        }

        int i = 0;
        ElapsedTime log = new ElapsedTime();

        for (Subsystem subsystem : this.robotSubsystems) {
            i++;
            subsystem.onCyclePassed();
            telemetry.addData("Loop times for " + i + "is: ", log.milliseconds());
            log.reset();
        }

        this.localizer.updatePose();




        /*
        telemetry.addData("Parallel Encoder 1 Ticks: ", this.drivetrain.rightBackMotor.getCurrentPosition());
        telemetry.addData("Parallel Encoder 2 Ticks: ", this.drivetrain.leftBackMotor.getCurrentPosition());
        telemetry.addData("Perpendicular Encoder 1 Ticks: ", this.drivetrain.leftFrontMotor.getCurrentPosition());
*/

/*
        telemetry.addData("X: ", currentPose.getX());
        telemetry.addData("Y: ", currentPose.getY());
        telemetry.addData("Heading: ", currentPose.getHeading());*/

        /*
        TelemetryPacket packet = new TelemetryPacket();
        Canvas fieldOverlay = packet.fieldOverlay();


        fieldOverlay.setStrokeWidth(1);
        fieldOverlay.setStroke("#4CAF50");
        */
        // drawRobot(fieldOverlay, currentPose);


        double frameTime = frameTimer.milliseconds();
        frameTimer.reset();

        return frameTime;
    }

    public void pause(double seconds) {
        ElapsedTime elapsedTime = new ElapsedTime();
        elapsedTime.reset();

        while (elapsedTime.seconds() < seconds) {
            this.update();
        }
    }

    public void persistData() {
        PersistentData.startPose = this.localizer.getPose();
    }

    public double getVoltage() {
        return this.voltageSensor.getVoltage();
    }

    public double getPowerMultiple() {
        return 12 / this.voltageCompensator;
    }

    /*
    public static void drawRobot(Canvas canvas, Pose2d pose) {
        canvas.strokeCircle(pose.getX(), pose.getY(), 9);
        Vector2d v = pose.headingVec().times(9);
        double x1 = pose.getX() + v.getX() / 2, y1 = pose.getY() + v.getY() / 2;
        double x2 = pose.getX() + v.getX(), y2 = pose.getY() + v.getY();
        canvas.strokeLine(x1, y1, x2, y2);
    }


    public void clearPersistData() {
        PersistentData.startPose = new Pose2d();
    }*/

    public void destroy() {
        RobotEx.robotInstance = null;
        internalIMU.destroy();
    }
}