package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.utilities.robot.RobotEx;
import org.firstinspires.ftc.teamcode.utilities.robot.subsystems.Intake;

/**
 * Example teleop code for a basic mecanum drive
 */

@TeleOp(name = "Template Teleop")
public class TemplateTeleop extends LinearOpMode {

    // Create new Instance of the robot
    RobotEx robot = RobotEx.getInstance();

    @Override
    public void runOpMode() {

        // Initialize the robot
        robot.init(hardwareMap, telemetry);

        waitForStart();

        // Notify subsystems before loop
        robot.postInit();

        if (isStopRequested()) return;

        // Initialize variables for loop
        Gamepad currentFrameGamepad1 = new Gamepad();
        Gamepad currentFrameGamepad2 = new Gamepad();

        Gamepad previousFrameGamepad1 = new Gamepad();
        Gamepad previousFrameGamepad2 = new Gamepad();

        boolean gripState = false;
        boolean rotationState = false;
        // robot.drivetrain.enableAntiTip();

        robot.update();

        while(opModeIsActive()) {

            // Retain information about the previous frame's gamepad
            previousFrameGamepad1.copy(currentFrameGamepad1);
            previousFrameGamepad2.copy(currentFrameGamepad2);

            currentFrameGamepad1.copy(gamepad1);
            currentFrameGamepad2.copy(gamepad2);


            telemetry.update();

            if (currentFrameGamepad1.left_trigger > 0) {
                robot.depositLift.driveLiftFromGamepad(
                        -currentFrameGamepad1.left_trigger
                );
            } else {
                robot.depositLift.driveLiftFromGamepad(
                        currentFrameGamepad1.right_trigger
                );
            }

            if (currentFrameGamepad2.left_trigger > 0) {
                robot.climbLift.setLiftPower(
                        -currentFrameGamepad2.left_trigger
                );
            } else {
                robot.climbLift.setLiftPower(
                        currentFrameGamepad2.right_trigger
                );
            }

            if (currentFrameGamepad1.a && !previousFrameGamepad1.a) {
                gripState = !gripState;
                robot.intake.setGripperState(
                        gripState ? Intake.GripperStates.OPEN : Intake.GripperStates.CLOSED
                );
            }

            if (currentFrameGamepad1.b && !previousFrameGamepad1.b) {
                rotationState = !rotationState;
                robot.intake.setRotationState(
                        rotationState ? Intake.RotationStates.ROTATED : Intake.RotationStates.DEFAULT
                );
            }

            robot.drivetrain.robotCentricDriveFromGamepad(
                    currentFrameGamepad1.left_stick_y,
                    currentFrameGamepad1.left_stick_x,
                    currentFrameGamepad1.right_stick_x
            );


            double frameTime = robot.update();

            telemetry.addData("Frame Time: ", frameTime);
        }
    }
}
