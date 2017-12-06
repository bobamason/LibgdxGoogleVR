package org.masonapps.libgdxgvr.utils;

import android.graphics.Bitmap;
import android.util.Log;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.BufferUtils;
import com.google.vr.sdk.base.Eye;

import org.masonapps.libgdxgvr.vr.VrApplicationAdapter;

import java.nio.ByteBuffer;

/**
 * Created by Bob on 10/28/2016.
 */

public class Screenshot360Utils {

    public static Bitmap generate360Screenshot(VrApplicationAdapter vrApplicationAdapter, int outWidth) {
        final Screenshot360 screenshot360 = new Screenshot360(vrApplicationAdapter, outWidth, 0, false);
//        try {
        return screenshot360.generate360Screenshot();
    }

    public static Bitmap generate360SteroscopicScreenshot(VrApplicationAdapter vrApplicationAdapter, int outWidth, float IPD) {
        final Screenshot360 screenshot360 = new Screenshot360(vrApplicationAdapter, outWidth, IPD, true);
//        try {
        return screenshot360.generate360Screenshot();
//        } catch (Exception e){
//            e.printStackTrace();
//        }
//        return null;
    }

    private static class Screenshot360 {

        private static final String TAG = Screenshot360.class.getSimpleName();
        final byte[] byteArray;
        private final int outWidth;
        private final int outHeight;
        private final int sliceHeight;
        private final ByteBuffer pixelBuffer;
        private float IPD;
        private boolean isStereoscopic;
        private VrApplicationAdapter vrApplicationAdapter;
        private PerspectiveCamera cam;
        private Ray ray;
        private Vector3 vrCamPosition = new Vector3();

        private Screenshot360(VrApplicationAdapter vrApplicationAdapter, int outWidth, float IPD, boolean isStereoscopic) {
            this.vrApplicationAdapter = vrApplicationAdapter;
            this.outWidth = outWidth;
            this.IPD = IPD;
            this.isStereoscopic = isStereoscopic;
            cam = new PerspectiveCamera();
            outHeight = outWidth / 2;
            sliceHeight = outHeight / 2;
//            Log.d(TAG, "sliceHeight: " + sliceHeight);
            ray = new Ray();
            pixelBuffer = BufferUtils.newByteBuffer(sliceHeight * 4);
            byteArray = new byte[sliceHeight * 4];
        }

        public Bitmap generate360Screenshot() {
            cam.far = vrApplicationAdapter.getVrCamera().far;
            cam.near = vrApplicationAdapter.getVrCamera().near;
            cam.viewportWidth = 1;
            cam.viewportHeight = sliceHeight;
            cam.fieldOfView = 90f;
            vrCamPosition.set(vrApplicationAdapter.getVrCamera().position);
            final Bitmap bitmap = Bitmap.createBitmap(outWidth, isStereoscopic ? outHeight * 2 : outHeight, Bitmap.Config.ARGB_8888);
            for (int column = 0; column < outWidth; column++) {
                if (column % 8 == 0)
                    Log.d(TAG, "screenshot progress " + (column * 100f / outWidth) + "%");
                final float x = (column + 0.5f) / (float) outWidth;

                if (isStereoscopic) {
                    updateRay(x, 0.25f, Eye.Type.LEFT);
                    renderSlice(bitmap, column, 0, Eye.Type.LEFT);

                    updateRay(x, 0.75f, Eye.Type.LEFT);
                    renderSlice(bitmap, column, sliceHeight, Eye.Type.LEFT);

                    updateRay(x, 0.25f, Eye.Type.RIGHT);
                    renderSlice(bitmap, column, outHeight, Eye.Type.RIGHT);

                    updateRay(x, 0.75f, Eye.Type.RIGHT);
                    renderSlice(bitmap, column, outHeight + sliceHeight, Eye.Type.RIGHT);
                } else {
                    updateRay(x, 0.25f, Eye.Type.MONOCULAR);
                    renderSlice(bitmap, column, 0, Eye.Type.MONOCULAR);

                    updateRay(x, 0.75f, Eye.Type.MONOCULAR);
                    renderSlice(bitmap, column, sliceHeight, Eye.Type.MONOCULAR);
                }
            }

            return bitmap;
        }

        private void updateRay(float x, float y, int eyeType) {
            final float theta = x * MathUtils.PI2 - MathUtils.PI;
            final float phi = MathUtils.PI * 0.5f - y * MathUtils.PI;
            float scale;
            switch (eyeType) {
                case Eye.Type.LEFT:
                    scale = -IPD / 2f;
                    break;
                case Eye.Type.RIGHT:
                    scale = IPD / 2f;
                    break;
                default:
                    scale = 0f;
                    break;
            }
            ray.origin.set((float) Math.cos(theta), 0, (float) Math.sin(theta)).scl(scale);
            ray.origin.add(vrCamPosition);
            ray.direction.set((float) (Math.sin(theta) * Math.cos(phi)), (float) Math.sin(phi), (float) (-Math.cos(theta) * Math.cos(phi)));
        }

        private void renderSlice(Bitmap bitmap, int column, int yOffset, int whichEye) {
            cam.position.set(ray.origin);
            cam.up.set(Vector3.Y);
            cam.lookAt(ray.origin.x + ray.direction.x, ray.origin.y + ray.direction.y, ray.origin.z + ray.direction.z);
            cam.update();
            Gdx.gl.glViewport(0, 0, 1, sliceHeight);
            this.vrApplicationAdapter.render(cam, whichEye);
            Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);
            Gdx.gl.glReadPixels(0, 0, 1, sliceHeight, GL20.GL_RGBA, GL20.GL_UNSIGNED_BYTE, pixelBuffer);
            pixelBuffer.get(byteArray);
            for (int y = 0; y < sliceHeight; y++) {
                final float phi = (y + 0.5f) / (float) sliceHeight * MathUtils.PI * 0.5f - MathUtils.PI * 0.25f;
                final float sampleY = sliceHeight - (float) ((Math.tan(phi) * 0.5 + 0.5)) * sliceHeight;
                bitmap.setPixel(column, Math.round(y) + yOffset, sampleSlice(sampleY, byteArray));
            }
        }

        private int sampleSlice(float sampleY, byte[] bytes) {
            int pixel, r, g, b, a;
            final int max = sliceHeight - 1;
            int i0 = MathUtils.clamp(MathUtils.floor(sampleY), 0, max);
            int i1 = MathUtils.clamp(MathUtils.ceil(sampleY), 0, max);
            final float w = sampleY - MathUtils.floor(sampleY);
            a = MathUtils.round(MathUtils.lerp(bytes[i0 * 4], bytes[i1 * 4], w));
            r = MathUtils.round(MathUtils.lerp(bytes[i0 * 4 + 3], bytes[i1 * 4 + 3], w));
            g = MathUtils.round(MathUtils.lerp(bytes[i0 * 4 + 2], bytes[i1 * 4 + 2], w));
            b = MathUtils.round(MathUtils.lerp(bytes[i0 * 4 + 1], bytes[i1 * 4 + 1], w));
            pixel = ((a & 0xff) << 24) | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
            return pixel;
        }
    }
}
