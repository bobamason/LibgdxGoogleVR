package org.masonapps.libgdxgvr.vr;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pools;
import com.google.vr.ndk.base.GvrApi;
import com.google.vr.sdk.controller.Controller;
import com.google.vr.sdk.proto.nano.Preferences;

import org.masonapps.libgdxgvr.GdxVr;

/**
 * Created by Bob on 6/8/2017.
 * based on GvrArmModel script in the Gvr Unity SDK
 */

public class ArmModel {
    public static final Quaternion pointerTilt = new Quaternion(Vector3.X, -15f);
    public static final Vector3 WORLD_FORWARD = new Vector3(0, 0, -1);
    public static final Vector3 POINTER_OFFSET = new Vector3(0.0f, -0.009f, -0.099f);
    private static final Vector3 DEFAULT_SHOULDER_RIGHT = new Vector3(0.19f, -0.19f, 0.03f);
    private static final Vector3 ELBOW_MIN_RANGE = new Vector3(-0.05f, -0.1f, -0.0f);
    private static final Vector3 ELBOW_MAX_RANGE = new Vector3(0.05f, 0.1f, -0.2f);
    private static final Vector3 ELBOW_POSITION = new Vector3(0.195f, -0.5f, 0.075f);
    private static final Vector3 WRIST_POSITION = new Vector3(0.0f, 0.0f, -0.25f);
    private static final Vector3 ARM_EXTENSION_OFFSET = new Vector3(-0.13f, 0.14f, -0.08f);

    private static final float MIN_EXTENSION_ANGLE = 7.0f;
    private static final float MAX_EXTENSION_ANGLE = 60.0f;

    private static final float EXTENSION_WEIGHT = 0.4f;
    private static ArmModel instance = null;
    public float addedElbowHeight = 0.0f;
    public float addedElbowDepth = 0.0f;
    public float fadeDistanceFromFace = 0.32f;
    public float tooltipMinDistanceFromFace = 0.45f;
    public int tooltipMaxAngleFromCamera = 80;
    public GazeBehavior followGaze = GazeBehavior.Always;
    public Vector3 pointerPosition = new Vector3();
    public Quaternion pointerRotation = new Quaternion();
    public Vector3 wristPosition = new Vector3();
    public Quaternion wristRotation = new Quaternion();
    public Vector3 elbowPosition = new Vector3();
    public Quaternion elbowRotation = new Quaternion();
    public Vector3 shoulderPosition = new Vector3();
    public Quaternion shoulderRotation = new Quaternion();
    public float preferredAlpha = 1f;
    public float tooltipAlphaValue = 1f;
    public OnArmModelUpdateEventListener listener;
    private Vector3 torsoDirection = new Vector3();
    private boolean firstUpdate;
    private Vector3 handedMultiplier = new Vector3();
    private Vector3 cameraForward = new Vector3();

    private ArmModel() {
        updateHandedness();
        firstUpdate = true;
    }

    public static ArmModel getInstance() {
        if (instance == null) {
            instance = new ArmModel();
        }
        return instance;
    }

    void updateHeadDirection(Vector3 forward) {
        cameraForward.set(forward);
    }

    public void onControllerUpdate(Controller controller) {
        updateHandedness();
        updateTorsoDirection();
        applyArmModel(controller);
//        UpdateTransparency();
        updatePointer();

        firstUpdate = false;
        if (listener != null) {
            listener.onArmModelUpdate(this);
        }
    }

    private void updateHandedness() {
        // Update user handedness if the setting has changed
        if (GdxVr.app == null) return;
        final GvrApi gvrApi = GdxVr.app.getGvrApi();
        final int handedness = gvrApi != null ? gvrApi.getUserPrefs().getControllerHandedness() : Preferences.UserPrefs.Handedness.RIGHT_HANDED;

        // Determine handedness multiplier.
        handedMultiplier.set(0, 1, 1);
        if (handedness == Preferences.UserPrefs.Handedness.RIGHT_HANDED) {
            handedMultiplier.x = 1.0f;
        } else if (handedness == Preferences.UserPrefs.Handedness.LEFT_HANDED) {
            handedMultiplier.x = -1.0f;
        }

        // Place the shoulder in anatomical positions based on the height and handedness.
        shoulderRotation.idt();
        shoulderPosition.set(DEFAULT_SHOULDER_RIGHT).scl(handedMultiplier);
    }

    private void updateTorsoDirection() {
        // Ignore updates here if requested.
        if (followGaze == GazeBehavior.Never) {
            return;
        }

        Vector3 gazeDirection = Pools.obtain(Vector3.class).set(cameraForward);
        gazeDirection.y = 0.0f;
        gazeDirection.nor();

        if (followGaze == GazeBehavior.Always || firstUpdate) {
            torsoDirection = gazeDirection;
        } else if (followGaze == GazeBehavior.DuringMotion) {
//            float angularVelocity = controller..magnitude;
//            float gazeFilterStrength = MathUtils.clamp((angularVelocity - 0.2f) / 45.0f, 0.0f, 0.1f);
//            torsoDirection.slerp(gazeDirection, gazeFilterStrength);
        }

        shoulderRotation.setFromCross(WORLD_FORWARD, torsoDirection);
        shoulderPosition.mul(shoulderRotation);
        Pools.free(gazeDirection);
    }

    private void resetState() {
        firstUpdate = true;
    }

    private void applyArmModel(Controller controller) {
        // Find the controller's orientation relative to the player
        Quaternion controllerOrientation = Pools.obtain(Quaternion.class).set(shoulderRotation);
        controllerOrientation.mul(controller.orientation.x, controller.orientation.y, controller.orientation.z, controller.orientation.w);

        // Get the relative positions of the joints
        elbowPosition.set(ELBOW_POSITION).add(0.0f, addedElbowHeight, addedElbowDepth);
        elbowPosition.scl(handedMultiplier);
        wristPosition.set(WRIST_POSITION).scl(handedMultiplier);
        Vector3 armExtensionOffset = new Vector3(ARM_EXTENSION_OFFSET).scl(handedMultiplier);

        // Extract just the x rotation angle
        Vector3 controllerForward = Pools.obtain(Vector3.class).set(WORLD_FORWARD).mul(controllerOrientation);
        float xAngle = 90.0f - (float) Math.acos(Vector3.dot(controllerForward.x, controllerForward.y, controllerForward.z, 0, 1, 0));

        // Remove the z rotation from the controller
        Quaternion xyRotation = Pools.obtain(Quaternion.class).setFromCross(WORLD_FORWARD, controllerForward);

        // Offset the elbow by the extension
        float normalizedAngle = (xAngle - MIN_EXTENSION_ANGLE) / (MAX_EXTENSION_ANGLE - MIN_EXTENSION_ANGLE);
        float extensionRatio = MathUtils.clamp(normalizedAngle, 0.0f, 1.0f);
        elbowPosition.add(armExtensionOffset.scl(extensionRatio));

        // Calculate the lerp interpolation factor
        float totalAngle = xyRotation.getAngle();
        float lerpSuppresion = 1.0f - (float) Math.pow(totalAngle / 180.0f, 6);
        float lerpValue = lerpSuppresion * (0.4f + 0.6f * extensionRatio * EXTENSION_WEIGHT);

        // Apply the absolute rotations to the joints
        Quaternion lerpRotation = Pools.obtain(Quaternion.class).slerp(xyRotation, lerpValue).conjugate();
        elbowRotation.set(shoulderRotation).mul(lerpRotation).mul(controllerOrientation);
        wristRotation.set(shoulderRotation).mul(controllerOrientation);

        // Determine the relative positions
        elbowPosition.mul(shoulderRotation);
        wristPosition.set(wristPosition).mul(elbowRotation).add(elbowPosition);

        Pools.free(controllerForward);
        Pools.free(controllerOrientation);
        Pools.free(xyRotation);
        Pools.free(lerpRotation);
    }

    private void updatePointer() {
        // Determine the direction of the ray.
        pointerPosition.set(POINTER_OFFSET).mul(wristRotation).add(wristPosition);
        pointerRotation.set(wristRotation).mul(pointerTilt);
    }

    /// Represents when gaze-following behavior should occur.
    public enum GazeBehavior {
        Never,        /// The shoulder will never follow the gaze.
        DuringMotion, /// The shoulder will follow the gaze during controller motion.
        Always        /// The shoulder will always follow the gaze.
    }

//    private void UpdateTransparency() {
//        // Determine how vertical the controller is pointing.
//        float animationDelta = DELTA_ALPHA * Time.deltaTime;
//        float distToFace = Vector3.Distance(wristPosition, Vector3.zero);
//        if (distToFace < fadeDistanceFromFace) {
//            preferredAlpha = Mathf.Max(0.0f, preferredAlpha - animationDelta);
//        } else {
//            preferredAlpha = Mathf.Min(1.0f, preferredAlpha + animationDelta);
//        }
//
//        float dot = Vector3.dot(wristRotation * Vector3.up, -wristPosition.normalized);
//        float minDot = (tooltipMaxAngleFromCamera - 90.0f) / -90.0f;
//        if (distToFace < fadeDistanceFromFace
//                || distToFace > tooltipMinDistanceFromFace
//                || dot < minDot) {
//            tooltipAlphaValue = Mathf.Max(0.0f, tooltipAlphaValue - animationDelta);
//        } else {
//            tooltipAlphaValue = Mathf.Min(1.0f, tooltipAlphaValue + animationDelta);
//        }
//    }

    /// Event handler that occurs when the state of the ArmModel is updated.
    public interface OnArmModelUpdateEventListener {
        void onArmModelUpdate(ArmModel armModel);
    }
}
