package com.cyrilmottier.android.listviewtipsandtricks.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;

/**
 * A special {@link CheckBox} that does not turn into the pressed state when
 * when the parent is already pressed.
 *
 * @author Cyril Mottier
 */
public class DontPressWithParentCheckBox extends CheckBox {

    String callsign;

    public DontPressWithParentCheckBox(Context context) {
        super(context);
    }

    public DontPressWithParentCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DontPressWithParentCheckBox(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setPressed(boolean pressed) {
        // Make sure the parent is a View prior casting it to View
        if (pressed && getParent() instanceof View && ((View) getParent()).isPressed()) {
            return;
        }
        super.setPressed(pressed);
    }

    public String getCallsign() {
        return this.callsign;
    }

    public void setCallsign(String callsign) {
        this.callsign = callsign;
    }


}
