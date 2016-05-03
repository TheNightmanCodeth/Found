package com.diragi.found.Animators;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;

import com.diragi.found.R;

/**
 * Created by joe on 4/30/16.
 */

public class FollowButtonBehavior extends CoordinatorLayout.Behavior<Button> {

    private int startY;
    private int finalY;
    private float startWidth;
    private float endWidth;
    private float startH;
    private float finalH;
    private float startToolbarPosition;

    public FollowButtonBehavior(Context context, AttributeSet attributeSet) {
        if (attributeSet != null) {
            TypedArray a = context.obtainStyledAttributes(attributeSet, R.styleable.FollowButtonBehavior);
            startY = (int) a.getDimension(R.styleable.FollowButtonBehavior_startYPos, 0);
            startWidth = a.getDimension(R.styleable.FollowButtonBehavior_startW, 0);
            endWidth   = a.getDimension(R.styleable.FollowButtonBehavior_finalW, 0);
            startH     = a.getDimension(R.styleable.FollowButtonBehavior_startH, 0);
            finalH     = a.getDimension(R.styleable.FollowButtonBehavior_finalH, 0);
            a.recycle();
        }
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, Button child, View dependency) {
        return dependency instanceof Toolbar;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, Button child, View dependency) {
        //Init properties that have not already been initialized
        maybeInitProperties(child, dependency);
        // Set relevant values

        final int maxScrollDistance = (int) (startToolbarPosition);
        float percentExpanded = dependency.getY() / maxScrollDistance;
        // Move the Button
        float distanceMoved = ((startY - finalY) * (1f - percentExpanded)) + (child.getHeight()/2);
        child.setY(startY - distanceMoved);

        float wToSubtract = ((startWidth - endWidth) * percentExpanded);
        float hToSubtract = ((startH - finalH) * percentExpanded);

        CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
        lp.width = (int) (startWidth - wToSubtract);
        lp.height = (int) (startH - hToSubtract);
        child.setLayoutParams(lp);

        return true;
    }

    private void maybeInitProperties(Button child, View dependency) {
        if (startY == 0) {
            startY = (int) (dependency.getY());
        }

        if (finalY == 0) {
            finalY = (dependency.getHeight() / 2);
        }

        if (startToolbarPosition == 0) {
            startToolbarPosition = dependency.getY();
        }

        if (startWidth == 0) {
            startWidth = child.getWidth();
        }
    }
}
