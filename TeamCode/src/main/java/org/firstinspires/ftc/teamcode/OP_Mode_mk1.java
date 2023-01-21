package org.firstinspires.ftc.teamcode;

/*
         ____   ___  _____ _____      _    ____      _    ____  _____ ____   ____
        / ___| / _ \|  ___|_   _|    / \  |  _ \    / \  | __ )| ____/ ___| / ___|
        \___ \| | | | |_    | |     / _ \ | |_) |  / _ \ |  _ \|  _| \___ \| |
        ___) | |_| |  _|   | |    / ___ \|  _ <  / ___ \| |_) | |___ ___) | |___
        |____/ \___/|_|     |_|   /_/   \_\_| \_\/_/   \_\____/|_____|____/ \____|
        ____  _____ ______     _______ ____  ____
        |  _ \| ____|  _ \ \   / / ____|  _ \/ ___|
        | |_) |  _| | |_) \ \ / /|  _| | |_) \___ \
        |  __/| |___|  _ < \ V / | |___|  _ < ___) |
        |_|   |_____|_| \_\ \_/  |_____|_| \_\____/
         _____   ____  _____ ______     _______ ____  ____
        | ____| |  _ \| ____|  _ \ \   / / ____|  _ \/ ___|
        |  _|   | |_) |  _| | |_) \ \ / /|  _| | |_) \___ \
        | |___  |  __/| |___|  _ < \ V / | |___|  _ < ___) |
        |_____| |_|   |_____|_| \_\ \_/  |_____|_| \_\____/
         ____  _   _ ____  _____      _    ____  _____ ____  _____ _   _ _____  _
        |  _ \| | | |  _ \| ____|    / \  |  _ \| ____|  _ \| ____| \ | |_   _|/ \
        | |_) | | | | |_) |  _|     / _ \ | | | |  _| | |_) |  _| |  \| | | | / _ \
        |  _ <| |_| |  __/| |___   / ___ \| |_| | |___|  _ <| |___| |\  | | |/ ___ \
        |_| \_\\___/|_|   |_____| /_/   \_\____/|_____|_| \_\_____|_| \_| |_/_/   \_\
 */

/*
 * SOFT ARABESC PERVERS VERSIUNE 0.23b
 * AUTOR: VERICU
 * E PERVERS, RUPE ADERENTA
 *
 * Cu de toate fara ceapa boss
 */

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.arcrobotics.ftclib.util.Timing;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.util.ElapsedTime;

@Config
class ThreadInfo {
    public static int target = 0;
    public static boolean shouldClose = false;
    public static boolean use = true;
    public static int fr;
}

@Config
class ArmcPIDF implements Runnable {
    DcMotorEx ridicareSlide;

    public ArmcPIDF(DcMotorEx ridicareSlide) {
        this.ridicareSlide = ridicareSlide;
        ridicareSlide.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        ridicareSlide.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public static double ppd = 0.000;
    public static double ppu = 0.00001;
    public static double pd = 0.001;
    public static double pu = 0.008;
    public static double d  = 0;
    public static double i  = 0;
    public static double Kf = 0.0005;

    double error = 0;
    double derivate = 0;
    double lastError = 0;
    double integralSum = 0;

    public void run() {
        //ElapsedTime timer  = new ElapsedTime();
        ElapsedTime timer2 = new ElapsedTime();
        double outp = 0;
        int tc = 0;
        //timer.reset();
        timer2.reset();
        FtcDashboard dashboard = FtcDashboard.getInstance();
        while (!ThreadInfo.shouldClose) {
            TelemetryPacket pack = new TelemetryPacket();
            pack.put("Target", ThreadInfo.target);
            pack.put("Current", ridicareSlide.getCurrentPosition());
            pack.put("Power", outp);
            dashboard.sendTelemetryPacket(pack);

            ++tc;
            if (timer2.seconds() >= 1.0) {
                ThreadInfo.fr = tc;
                tc = 0;
                timer2.reset();
            }
            if (ThreadInfo.use) {
                error = ThreadInfo.target - ridicareSlide.getCurrentPosition();
                /*derivate = (error - lastError) / timer.seconds();
                integralSum = integralSum + (error * timer.seconds());*/
                if (error < -220) {
                    outp = (ppd * error * error) + (pd * error) + (d * derivate) + (i * integralSum) + Kf;
                } else {
                    outp = (ppu * error * error) + (pu * error) + (d * derivate) + (i * integralSum) + Kf;
                }
                ridicareSlide.setPower(outp);

                lastError = error;
                //timer.reset();
            } else {
                error = derivate = lastError = integralSum = 0;
            }
            /*try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/
        }
    }
}

@Config
@TeleOp
public class OP_Mode_mk1 extends LinearOpMode {
    //sasiu
    public DcMotorEx leftBack;
    public DcMotorEx leftFront;
    public DcMotorEx rightBack;
    public DcMotorEx rightFront;
    //restu
    public DcMotorEx ridicareSlide;
    public Servo s1;
    public Servo s2;
    int poz = 0;

    public static boolean USE_TELEMETRY = false;

    int lpos = 0;

    //

    boolean L2A = false;
    boolean L2B = false;
    boolean L2U = false;
    boolean L2D = false;
    boolean G2X = false;
    boolean R2RB = false;
    boolean R2LB = false;
    boolean R2LT = false;
    boolean RB = false;
    boolean switched = false;

    public static double AUT_POW = 0.811111212;
    public static double POW_COEF = -0.9;
    public static double COB_COEF = 0.5;
    public static boolean OV3RDR1V3 = false;

    public static int BRAK = 1;

    public static int dif = 170;

    public static int TOP_POS = 1303;
    public static int MIU_POS = 1010;
    public static int MID_POS = 760;

    public static double SDESCHIS = 0.58;
    public static double SINCHIS = 0.362;
    public static double s1pos = SDESCHIS;

    public void runOpMode() {
        VoltageSensor batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();

        //SampleMecanumDrive drive = new SampleMecanumDrive(hardwareMap);

        L2A = L2B = L2U = L2D = G2X = R2RB = R2LB = R2LT = RB = switched = OV3RDR1V3 = false;

        leftBack = hardwareMap.get(DcMotorEx.class, "LB");
        leftFront = hardwareMap.get(DcMotorEx.class, "LF");
        rightBack = hardwareMap.get(DcMotorEx.class, "RB");
        rightFront = hardwareMap.get(DcMotorEx.class, "RF");
        ridicareSlide = hardwareMap.get(DcMotorEx.class, "RS");

        Runnable armRun = new ArmcPIDF(ridicareSlide);
        Thread armThread = new Thread(armRun);

        s1 = hardwareMap.get(Servo.class, "S1");
        //s2 = hardwareMap.get(Servo.class, "S2");
        leftBack.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightBack.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        leftFront.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        rightFront.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);

        if (BRAK == 1) {
            rightBack.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
            rightFront.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
            leftBack.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
            leftFront.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        }

        ridicareSlide.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
        // ridicareSlide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        ridicareSlide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        ridicareSlide.setDirection(DcMotorEx.Direction.REVERSE);
        ridicareSlide.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        //reverse those suckers
        leftBack.setDirection(DcMotorEx.Direction.FORWARD);
        leftFront.setDirection(DcMotorEx.Direction.FORWARD);
        rightBack.setDirection(DcMotorEx.Direction.REVERSE);
        rightFront.setDirection(DcMotorEx.Direction.REVERSE);

        Servo SPa = hardwareMap.get(Servo.class, "SPa");
        Servo SPe = hardwareMap.get(Servo.class, "SPe");

        SPa.setPosition(0.0);
        SPe.setPosition(0.0);

        s1pos = SDESCHIS;

        waitForStart();

        ThreadInfo.shouldClose = false;
        armThread.start();
        ThreadInfo.use = true;

        /*TelemetryPacket pack;
        FtcDashboard dashboard = FtcDashboard.getInstance();
        while (opModeIsActive()) {
            ThreadInfo.use = true;
        }*/

        while (opModeIsActive()) {
            double speed = Math.hypot(gamepad1.left_stick_x, gamepad1.left_stick_y);
            double angle = Math.atan2(gamepad1.left_stick_y, gamepad1.left_stick_x) - Math.PI / 4;
            double turn = -gamepad1.right_stick_x;
            //maths (nu stiu eu deastea ca fac cu antohe)
            final double lfPower = speed * Math.sin(angle) + turn;
            final double rfPower = (speed * Math.cos(angle) - turn);
            final double lbPower = speed * Math.cos(angle) + turn;
            final double rbPower = (speed * Math.sin(angle) - turn);

            if (!L2A && gamepad2.a) {
                ThreadInfo.target = TOP_POS;
                //ridicareSlide.setTargetPosition(TOP_POS);
            }
            L2A = gamepad2.a;

            if (!L2B && gamepad2.b) {
                ThreadInfo.target = 0;
                //ridicareSlide.setTargetPosition(0);
            }
            L2B = gamepad2.b;

            if (!L2U && gamepad2.dpad_up) {
                ThreadInfo.target = MIU_POS;
                //ridicareSlide.setTargetPosition(MIU_POS);
            }
            L2U = gamepad2.dpad_up;

            if (!L2D && gamepad2.dpad_down) {
                ThreadInfo.target = MID_POS;
                //ridicareSlide.setTargetPosition(MID_POS);
            }
            L2D = gamepad2.dpad_down;

            if (!R2RB && gamepad2.right_bumper &&
                    !R2LB && gamepad2.left_bumper
            ) {
                OV3RDR1V3 = !OV3RDR1V3;
                telemetry.addLine("Nothing here, keep looking");
                telemetry.update();
            }
            R2RB = gamepad2.right_bumper;
            R2LB = gamepad2.left_bumper;

            if (!R2LT && gamepad2.left_trigger > 0.01) {
                USE_TELEMETRY = !USE_TELEMETRY;
                telemetry.addLine("Nothing here, keep looking");
                telemetry.update();
            }
            R2LT = gamepad2.left_trigger > 0.01;

            if (USE_TELEMETRY) {
                telemetry.clear();
                telemetry.clearAll();
                telemetry.addLine("PT");
                //drive.updatePoseEstimate();
                //telemetry.addData("Cpos", drive.getPoseEstimate());
            }
            if (OV3RDR1V3) {
                if (USE_TELEMETRY) {
                    telemetry.addLine("ALL 0V3");
                }
                if (Math.abs(gamepad2.right_stick_y) > 0.01) {
                    ThreadInfo.use = false;
                    if (ridicareSlide.getMode() != DcMotorEx.RunMode.RUN_USING_ENCODER) {
                        ridicareSlide.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
                        ridicareSlide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
                    }
                    if (USE_TELEMETRY) {
                        telemetry.addLine("0V3RDR1V3 MAN");
                    }
                    ridicareSlide.setPower(POW_COEF * gamepad2.right_stick_y);
                } else {
                    ThreadInfo.use = true;
                    /*if (ridicareSlide.getMode() != DcMotorEx.RunMode.RUN_TO_POSITION) {
                        ridicareSlide.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
                    }*/
                    if (USE_TELEMETRY) {
                        telemetry.addLine("0V3RDR1V3 AUT");
                    }

                    ThreadInfo.target = 0;
                    /*ridicareSlide.setTargetPosition(0);
                    ridicareSlide.setPower(POW_COEF / 2);*/
                }
            } else {
                if (Math.abs(gamepad2.right_stick_y) > 0.01) {
                    ThreadInfo.use = false;
                    if (USE_TELEMETRY) {
                        telemetry.addLine("ALL MAN");
                    }
                    /*if (ridicareSlide.getMode() != DcMotorEx.RunMode.RUN_USING_ENCODER) {
                        ridicareSlide.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
                    }*/
                    lpos = ridicareSlide.getCurrentPosition();
                    if (lpos < 0) {
                        if (USE_TELEMETRY) {
                            telemetry.addLine("MAN UND");
                        }
                        ThreadInfo.target = 10;
                        //ridicareSlide.setPower(AUT_POW);
                        //ridicareSlide.setTargetPosition(10);
                    } else if (lpos < 150 && gamepad2.right_stick_y > 0) {
                        if (USE_TELEMETRY) {
                            telemetry.addLine("MAN CLOSE UND");
                        }
                        ridicareSlide.setPower(0);
                        ThreadInfo.target = lpos;
                        //ridicareSlide.setTargetPosition(lpos);
                    } else if (lpos > TOP_POS - 40 && gamepad2.right_stick_y < 0) {
                        if (USE_TELEMETRY) {
                            telemetry.addLine("MAN CLOSE UPW");
                        }
                        ridicareSlide.setPower(0);
                        ThreadInfo.target = lpos;
                        //ridicareSlide.setTargetPosition(lpos);
                    } else if (lpos < TOP_POS) {
                        if (gamepad2.right_stick_y > 0) {
                            if (USE_TELEMETRY) {
                                telemetry.addLine("COB MAN");
                            }
                            ridicareSlide.setPower(-COB_COEF * 1.3);
                        } else {
                            if (USE_TELEMETRY) {
                                telemetry.addLine("RID MAN");
                            }
                            ridicareSlide.setPower(POW_COEF * gamepad2.right_stick_y);
                        }
                        ThreadInfo.target = lpos;
                        //ridicareSlide.setTargetPosition(lpos);
                    } else {
                        if (USE_TELEMETRY) {
                            telemetry.addLine("MAN UPW");
                        }
                        ridicareSlide.setPower(AUT_POW);
                        ThreadInfo.target = TOP_POS;
                        //ridicareSlide.setTargetPosition(TOP_POS);
                    }
                } else {
                    ThreadInfo.use = true;
                    /*if (ridicareSlide.getMode() != DcMotorEx.RunMode.RUN_TO_POSITION) {
                        ridicareSlide.setMode(DcMotorEx.RunMode.RUN_TO_POSITION);
                    }*/
                    if (USE_TELEMETRY) {
                        telemetry.addLine("ALL AUT");
                    }
                    //if (ridicareSlide.getTargetPosition() < ridicareSlide.getCurrentPosition()) {
                    if (ThreadInfo.target < ridicareSlide.getCurrentPosition()) {
                        if (USE_TELEMETRY) {
                            telemetry.addLine("COB AUT");
                        }
                        //ridicareSlide.setPower(COB_COEF);
                    } else {
                        if (USE_TELEMETRY) {
                            telemetry.addLine("RID AUT");
                        }
                        //ridicareSlide.setPower(AUT_POW);
                    }
                }
            }
            if (USE_TELEMETRY) {
                telemetry.addLine("TEST");
            }

            if (!RB && gamepad2.left_bumper) {
                //ridicareSlide.setPower(1.0);
                ThreadInfo.target = ThreadInfo.target - dif;
                //ridicareSlide.setTargetPosition(ridicareSlide.getCurrentPosition() - dif);
            }
            if (RB && !gamepad2.left_bumper) {
                //ridicareSlide.setPower(1.0);
                ThreadInfo.target = ThreadInfo.target + dif;
                //ridicareSlide.setTargetPosition(ridicareSlide.getCurrentPosition() + dif);
            }
            RB = gamepad2.left_bumper;

            if (USE_TELEMETRY) {
                if (OV3RDR1V3) {
                    telemetry.addLine("U HAV3 3NT3R3D 0V3RDR1V3 M0D3");
                }
                telemetry.addData("Ridicare", ridicareSlide.getCurrentPosition());
                telemetry.addData("Target", ThreadInfo.target);//ridicareSlide.getTargetPosition());
                telemetry.addData("Cpow", ridicareSlide.getPower());
                telemetry.addData("left_stick_y", poz);
                telemetry.addData("r_stick_y", gamepad2.right_stick_y);
                telemetry.addData("pad", gamepad1.right_trigger);
                telemetry.addData("Fps", ThreadInfo.fr);
                telemetry.update();
            } else if (OV3RDR1V3) {
                telemetry.addLine("U HAV3 3NT3R3D 0V3RDR1V3 M0D3");
                telemetry.update();
            }

            if (!G2X && gamepad2.x) {
                if (switched) {
                    s1pos = SDESCHIS;
                } else {
                    s1pos = SINCHIS;
                }
                switched = !switched;
            }
            G2X = gamepad2.x;
            s1.setPosition(s1pos);



            /*if (Math.abs(gamepad1.left_stick_x) +
                Math.abs(gamepad1.left_stick_y) +
                Math.abs(gamepad1.right_stick_x) +
                Math.abs(gamepad1.right_stick_y) > 0.0001) {
            } else {*/
            //dam blana in motoare
            double pcoef = 12.0 / batteryVoltageSensor.getVoltage();
            double spcoef = 1 - 0.6 * gamepad1.right_trigger;
            double fcoef = pcoef * spcoef;
            leftFront.setPower(lfPower * fcoef);
            rightFront.setPower(rfPower * fcoef);
            leftBack.setPower(lbPower * fcoef);
            rightBack.setPower(rbPower * fcoef);
            //pep = SPe.getPosition();
            //pap = SPa.getPosition();
            //}
        }

        try {
            ThreadInfo.shouldClose = true;
            armThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}