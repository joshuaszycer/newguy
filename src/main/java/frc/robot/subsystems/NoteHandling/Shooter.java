package frc.robot.subsystems.NoteHandling;


import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.networktables.GenericEntry;
import edu.wpi.first.wpilibj.shuffleboard.Shuffleboard;
// Imports go here
import edu.wpi.first.wpilibj2.command.SubsystemBase;

import static frc.robot.Constants.ShooterConstants.*;

import java.util.function.DoubleSupplier;

import static frc.robot.Constants.*;
import static frc.robot.Constants.PivotConstants.kCurrentLimit;
import static frc.robot.Constants.PivotConstants.kPivotErrorTolerance;

import com.ctre.phoenix6.configs.CurrentLimitsConfigs;
import com.ctre.phoenix6.configs.MotorOutputConfigs;
import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.*;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import com.ctre.phoenix6.controls.*;

// FOLLOW ALONG THIS DOCUMENTATION: https://docs.google.com/document/d/143tNsvYQFAErQTJDxO9d1rwM7pv80vpLfLK-WiIEOiw/edit?tab=t.0

public class Shooter extends SubsystemBase {

    public enum ShooterStates{
        // MAKE STATES
        // some considerations: off state, states for shooter at each type of scoring location, and a transition state between states

        // ||||||||||||||||||||||||||||||||
        StateOff,
        StateAmp,
        StateSpeaker,
        StateTrans,
        StatePass //the cool one
    }

    public static ShooterStates m_shooterRequestedState;
    public static ShooterStates m_shooterCurrentState;
 
    // CREATE TALON MOTORS HERE
    // the shooter has two talon motors on it, have fun

    TalonFX m_ShooterL = new TalonFX(kShooterLeftPort, "Mast");
    TalonFX m_ShooterR = new TalonFX(kShooterRightPort, "Mast");

    MotionMagicVelocityVoltage request = new MotionMagicVelocityVoltage(0).withSlot(0).withEnableFOC(true);

    // ||||||||||||||||||||||||||||||||

    private double desiredVelocity = 0;
    private double desiredVoltage = 0;

    // you might notice a new type right below here called a "DoubleSupplier," don't worry about it, you won't need to use distanceFromSpeaker for this
    // incase you were wonder though, it is a lambda, cause of course it is
    public Shooter(DoubleSupplier distanceFromSpeaker) {

        // CREATE THE CONFIGURATIONS FOR THE TALONS HERE
        // talon configs are set up differently than sparks, please use the doc if you want to spare your sanity
        var talonFXConfigs = new TalonFXConfiguration();
        var motorOutputConfigs = talonFXConfigs.MotorOutput;
        var currentLimitsConfigs = talonFXConfigs.CurrentLimits;
        var feedbackConfigs = talonFXConfigs.Feedback;

        var motionMagic = talonFXConfigs.MotionMagic;
        var slot0Configs = talonFXConfigs.Slot0;
        //i love OOP but why make it do this
        //better than lambdas though
        //i hate lambdas 

       
  
        currentLimitsConfigs.StatorCurrentLimit = kCurrentLimit;
        currentLimitsConfigs.StatorCurrentLimitEnable = true;

        currentLimitsConfigs.SupplyCurrentLimit = kCurrentLimit;
        currentLimitsConfigs.SupplyCurrentLimitEnable = true;

        feedbackConfigs.SensorToMechanismRatio = kSensorToMechanismGearRatio;

        slot0Configs.kS = kSShooter;
        slot0Configs.kV = kVShooter;
        slot0Configs.kA = kAShooter;
        slot0Configs.kP = kPShooter;
        slot0Configs.kI = kIShooter;
        slot0Configs.kD = kDShooter;

        motionMagic.MotionMagicCruiseVelocity = kShooterCruiseVelocity;
        motionMagic.MotionMagicAcceleration = kShooterAcceleration;
        motionMagic.MotionMagicJerk = kShooterJerk;

        //request.withEnableFOC(true);

        //taken from the doc,
        //"cool tip, the .withEnableFOC() method just makes motion magic better"
        //i dont know anything about it but im just gonna trust it lolol
        //^ it broke everything for a hot second

        // ||||||||||||||||||||||||||||||||

        motorOutputConfigs.NeutralMode = NeutralModeValue.Coast;
        
        motorOutputConfigs.Inverted = InvertedValue.Clockwise_Positive;
        m_ShooterL.getConfigurator().apply(talonFXConfigs);

        motorOutputConfigs.Inverted = InvertedValue.CounterClockwise_Positive;
        m_ShooterR.getConfigurator().apply(talonFXConfigs);

        // give some default state to these guys
        // m_shooterCurrentState;
        // m_shooterRequestedState;

        m_shooterCurrentState = ShooterStates.StateOff;
        m_shooterRequestedState = m_shooterCurrentState;
    }
        
    @Override
    public void periodic() {


        // SWITCH/IF STATEMENT GOES HERE

        switch(m_shooterRequestedState) {
          case StateOff:
            desiredVelocity = 0;
            break;
          case StateAmp:
            desiredVelocity = 8;
            break;
          case StateSpeaker:
            desiredVelocity = 10;
            break;
          case StatePass:
            desiredVelocity = 5;
            break;
          case StateTrans:
            //shouldnt this Not change the desired velocity?
            //because it like. keeps the same velocity as before?
            //idk :shrug:

            //not checking for this gives the warning
            //"The enum constant StateTrans needs a corresponding case 
            //label in this enum switch on Shooter.ShooterStatesJava"
            break;
        }

        // ||||||||||||||||||||||||||||||||
     
        runControlLoop();
    
        // ERROR CHECKING GOES HERE

        if(getError() > kShooterErrorTolerance) {
          m_shooterCurrentState = ShooterStates.StateTrans;
        } else {
          m_shooterCurrentState = m_shooterRequestedState;
        }

        // ||||||||||||||||||||||||||||||||

    }

      public void runControlLoop() {
        // SHOOTER SHENANIGANS GO HERE UNLESS YOU ARE TOO COOL FOR THAT

        m_ShooterL.setControl(request);

        // ||||||||||||||||||||||||||||||||
      }
    
      // SO MANY METHODS TO MAKE (like 4), SO LITTLE TIME TO DO IT (literally 6 hours)


      // HOW WILL WE EVER DO IT?? (half of them were just changing them to return something)

      public double getVelocity() {
        return m_ShooterL.getVelocity().getValue();
      }
    
      public double getError() {
        return Math.abs(desiredVelocity - request.Velocity);
      }
     
      public void requestState(ShooterStates requestedState) {
        m_shooterRequestedState = requestedState;
      }
     
      public ShooterStates getCurrentState() {
        return m_shooterCurrentState;
      }

        // ||||||||||||||||||||||||||||||||

    }

    