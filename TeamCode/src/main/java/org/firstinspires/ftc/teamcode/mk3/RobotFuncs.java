package org.firstinspires.ftc.teamcode.mk3;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.hardwareMap;
import static org.firstinspires.ftc.teamcode.RobotVars.EMAX;
import static org.firstinspires.ftc.teamcode.RobotVars.EMIN;
import static org.firstinspires.ftc.teamcode.RobotVars.IN_TESTING;
import static org.firstinspires.ftc.teamcode.RobotVars.RBOT_POS;
import static org.firstinspires.ftc.teamcode.RobotVars.RTOP_POS;
import static org.firstinspires.ftc.teamcode.RobotVars.S1PO;
import static org.firstinspires.ftc.teamcode.RobotVars.S2PO;
import static org.firstinspires.ftc.teamcode.RobotVars.S3PO;
import static org.firstinspires.ftc.teamcode.RobotVars.SAH;
import static org.firstinspires.ftc.teamcode.RobotVars.SBH;
import static org.firstinspires.ftc.teamcode.RobotVars.SCC;
import static org.firstinspires.ftc.teamcode.RobotVars.SCO;
import static org.firstinspires.ftc.teamcode.RobotVars.SINCHIS;
import static org.firstinspires.ftc.teamcode.RobotVars.TESTINGAB;
import static org.firstinspires.ftc.teamcode.RobotVars.USE_PHOTON;
import static org.firstinspires.ftc.teamcode.RobotVars.coneClaw;
import static org.firstinspires.ftc.teamcode.RobotVars.coneReady;
import static org.firstinspires.ftc.teamcode.RobotVars.ebp;
import static org.firstinspires.ftc.teamcode.RobotVars.ed;
import static org.firstinspires.ftc.teamcode.RobotVars.ei;
import static org.firstinspires.ftc.teamcode.RobotVars.ep;
import static org.firstinspires.ftc.teamcode.RobotVars.pcoef;
import static org.firstinspires.ftc.teamcode.RobotVars.rbp;
import static org.firstinspires.ftc.teamcode.RobotVars.rd;
import static org.firstinspires.ftc.teamcode.RobotVars.ri;
import static org.firstinspires.ftc.teamcode.RobotVars.rp;

import com.acmerobotics.dashboard.FtcDashboard;
import com.outoftheboxrobotics.photoncore.PhotonCore;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.VoltageSensor;

@SuppressWarnings("ALL")
public class RobotFuncs {
    public static DcMotorEx ridA, ridB;
    public static DcMotorEx extA, extB;
    public static Servo sextA, sextB;
    public static Servo sClose, sHeading, sBalans, sMCLaw;
    public static Servo S1, S2, S3;
    public static PIDF epd, rpd;
    public static Clown clo;
    public static BNO055IMU imu;
    public static DcMotorEx leftBack, leftFront, rightBack, rightFront;
    public static VoltageSensor batteryVoltageSensor;
    public static FtcDashboard dashboard;

    static void ep(double p) {
        extA.setPower(p);
        extB.setPower(p);
    }

    static void rp(double p) {
        ridA.setPower(p);
        ridB.setPower(p);
    }

    static void spe(boolean er, double p) {
        if (!er) {
            if (p == 0) {
                epd.use = true;
            } else {
                epd.use = false;
                if (extA.getCurrentPosition() < EMIN) {
                    ep(0);
                    return;
                }
                if (extA.getCurrentPosition() > EMAX) {
                    ep(0);
                    return;
                }
                ep(p);
            }
        } else {
            if (p == 0) {
                rpd.use = true;
            } else {
                rpd.use = false;
                if (ridA.getCurrentPosition() < RBOT_POS) {
                    rp(0);
                    return;
                }
                if (ridA.getCurrentPosition() > RTOP_POS) {
                    rp(0);
                    return;
                }
                rp(p);
            }
        }
    }

    static void rid(int pos) {
        if (coneReady || pos == RBOT_POS) {
            if (pos == RBOT_POS) {
                sMCLaw.setPosition(SCO);
                coneReady = false;
            }
            rpd.set_target(pos, false);
        } else {
            clo.toGet = true;
        }
    }

    static void prep_cone() {
        sClose.setPosition(SINCHIS);
        sextA.setPosition(SAH);
        sextB.setPosition(SBH);
        coneClaw = true;
    }

    static void ext(int pos) {
        if (pos == EMAX) {
            clo.toGet = true;
            coneReady = false;
        } else {
            prep_cone();
        }
        rid(RBOT_POS);
        epd.set_target(pos, false);
    }

    static DcMotorEx initm(String s, boolean e, boolean r) {
        DcMotorEx m = hardwareMap.get(DcMotorEx.class, s);
        if (e) {
            m.setMode(DcMotorEx.RunMode.STOP_AND_RESET_ENCODER);
            m.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        } else {
            m.setMode(DcMotorEx.RunMode.RUN_WITHOUT_ENCODER);
        }
        m.setZeroPowerBehavior(DcMotorEx.ZeroPowerBehavior.BRAKE);
        m.setDirection(r ? DcMotorEx.Direction.REVERSE : DcMotorEx.Direction.FORWARD);
        return m;
    }

    static Thread extT, ridT, cloT;

    static void initma() {
        if (USE_PHOTON) {
            PhotonCore.enable();
            PhotonCore.experimental.setSinglethreadedOptimized(false);
        }

        dashboard = FtcDashboard.getInstance();
        batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();
        rightBack = initm("RB", false, false);
        rightFront = initm("RF", false, false);
        leftBack = initm("LB", false, true);
        leftFront = initm("LF", false, true);
        extA = initm("extA", true, true);
        extB = initm("extB", true, true);
        ridA = initm("ridA", true, true);
        ridB = initm("ridB", true, true);
        //underglow = hardwareMap.get(DcMotor.class, "Underglow"); You will not be forgotten

        sClose = hardwareMap.get(Servo.class, "sClose");
        sHeading = hardwareMap.get(Servo.class, "sHeading");
        sBalans = hardwareMap.get(Servo.class, "sBalans");
        sMCLaw = hardwareMap.get(Servo.class, "sMCLaw");
        if (IN_TESTING) {
            if (TESTINGAB) {
                sextA = sextB = hardwareMap.get(Servo.class, "sextA");
            } else {
                sextA = sextB = hardwareMap.get(Servo.class, "sextB");
            }
        } else {
            sextA = hardwareMap.get(Servo.class, "sextA");
            sextB = hardwareMap.get(Servo.class, "sextB");
        }

        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        /*parameters.mode = BNO055IMU.SensorMode.COMPASS;
        parameters.gyroPowerMode = BNO055IMU.GyroPowerMode.FAST;
        parameters.gyroBandwidth = BNO055IMU.GyroBandwidth.HZ523; /// TODO ???????
        parameters.gyroRange = BNO055IMU.GyroRange.DPS2000;*/
        imu.initialize(parameters);

        epd = new PIDF(extA, extB, ep, ed, ei, ebp);
        rpd = new PIDF(ridA, ridB, rp, rd, ri, rbp);
        clo = new Clown(sextA, sextB, sHeading, sClose, sMCLaw, sBalans, extA);

        extT = new Thread(epd);
        ridT = new Thread(rpd);
        cloT = new Thread(clo);

        S1 = hardwareMap.get(Servo.class, "SPe");
        S2 = hardwareMap.get(Servo.class, "SPa1");
        S3 = hardwareMap.get(Servo.class, "SPa2");
        S1.setPosition(S1PO);
        S2.setPosition(S2PO);
        S3.setPosition(S3PO);
    }

    public static void startma() {
        pcoef = 12.0 / batteryVoltageSensor.getVoltage();

        epd.shouldClose = false;
        epd.use = true;
        epd.target = 0;
        extT.start();

        rpd.shouldClose = false;
        rpd.use = true;
        rpd.target = 0;
        ridT.start();

        clo.shouldClose = true;
        cloT.start();
    }

    public static void endma() {
        leftFront.setPower(0);
        rightFront.setPower(0);
        leftBack.setPower(0);
        rightBack.setPower(0);
        ridA.setPower(0);
        ridB.setPower(0);
        extA.setPower(0);
        extB.setPower(0);
        imu.close();
        batteryVoltageSensor.close();
        epd.shouldClose = true;
        rpd.shouldClose = true;
        clo.shouldClose = true;
        try {
            extT.join(10);
            ridT.join(10);
            cloT.join(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}