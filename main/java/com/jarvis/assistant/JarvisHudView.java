package com.jarvis.assistant;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class JarvisHudView extends View {

    public enum State { IDLE, LISTENING, SPEAKING, THINKING }

    private State state = State.IDLE;
    private float rotation = 0f;
    private float pulse = 0f;
    private ValueAnimator animator;

    private Paint ringPaint;
    private Paint corePaint;
    private Paint arcPaint;

    public JarvisHudView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeWidth(4f);
        ringPaint.setColor(Color.parseColor("#33C6FF"));

        corePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        corePaint.setStyle(Paint.Style.FILL);
        corePaint.setColor(Color.parseColor("#66E0FF"));

        arcPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(6f);
        arcPaint.setStrokeCap(Paint.Cap.ROUND);
        arcPaint.setColor(Color.parseColor("#00FFF7"));

        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator a) {
                float t = (Float) a.getAnimatedValue();
                float speedMultiplier = state == State.LISTENING ? 3f
                        : state == State.THINKING ? 2f : 1f;
                rotation = (rotation + speedMultiplier * 2f) % 360f;
                pulse = (float) (0.5 + 0.5 * Math.sin(t * Math.PI * 2 * speedMultiplier));
                invalidate();
            }
        });
        animator.start();
    }

    public void setState(State newState) {
        this.state = newState;
        int coreColor;
        switch (newState) {
            case LISTENING:
                coreColor = Color.parseColor("#00FFC8");
                break;
            case SPEAKING:
                coreColor = Color.parseColor("#FFC800");
                break;
            case THINKING:
                coreColor = Color.parseColor("#B266FF");
                break;
            default:
                coreColor = Color.parseColor("#66E0FF");
                break;
        }
        corePaint.setColor(coreColor);
        arcPaint.setColor(coreColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float baseRadius = Math.min(getWidth(), getHeight()) / 3f;

        ringPaint.setAlpha(120);
        canvas.drawCircle(cx, cy, baseRadius * 1.4f, ringPaint);

        RectF arcRect = new RectF(cx - baseRadius * 1.15f, cy - baseRadius * 1.15f,
                cx + baseRadius * 1.15f, cy + baseRadius * 1.15f);
        arcPaint.setAlpha(220);
        canvas.drawArc(arcRect, rotation, 80, false, arcPaint);
        canvas.drawArc(arcRect, rotation + 120, 80, false, arcPaint);
        canvas.drawArc(arcRect, rotation + 240, 80, false, arcPaint);

        RectF midRect = new RectF(cx - baseRadius * 0.9f, cy - baseRadius * 0.9f,
                cx + baseRadius * 0.9f, cy + baseRadius * 0.9f);
        ringPaint.setAlpha(180);
        canvas.drawArc(midRect, -rotation * 1.5f, 40, false, ringPaint);
        canvas.drawArc(midRect, -rotation * 1.5f + 180, 40, false, ringPaint);

        float coreRadius = baseRadius * 0.35f * (0.85f + 0.15f * pulse);
        corePaint.setAlpha((int) (180 + 60 * pulse));
        canvas.drawCircle(cx, cy, coreRadius, corePaint);
    }

    public void destroy() {
        if (animator != null) animator.cancel();
    }
}
