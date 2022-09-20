package com.smart.planner;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.widget.ImageViewCompat;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;

public class ColorPicker extends DialogFragment {

    private Dialog dialog;
    private ImageView color1,color2,color3,color4,color5,color6,color7,color8,color9,color10,
    color11,color12,color13,color14,color15,color16,color17,color18,color19,color20 ;

    private HashMap<String, ImageView> labels = new HashMap<>();
//    private GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
//            new int[]{0xFFFFFF,0xFFFFFF});

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_color_picker, null);

        color1 = view.findViewById(R.id.color_1);
        color2 = view.findViewById(R.id.color_2);
        color3 = view.findViewById(R.id.color_3);
        color4 = view.findViewById(R.id.color_4);
        color5 = view.findViewById(R.id.color_5);
        color6 = view.findViewById(R.id.color_6);
        color7 = view.findViewById(R.id.color_7);
        color8 = view.findViewById(R.id.color_8);
        color9 = view.findViewById(R.id.color_9);
        color10 = view.findViewById(R.id.color_10);
        color11 = view.findViewById(R.id.color_11);
        color12 = view.findViewById(R.id.color_12);
        color13 = view.findViewById(R.id.color_13);
        color14 = view.findViewById(R.id.color_14);
        color15 = view.findViewById(R.id.color_15);
        color16 = view.findViewById(R.id.color_16);
        color17 = view.findViewById(R.id.color_17);
        color18 = view.findViewById(R.id.color_18);
        color19 = view.findViewById(R.id.color_19);
        color20 = view.findViewById(R.id.color_20);

        color1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color1);
            }
        });

        color2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color2);
            }
        });

        color3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color3);
            }
        });

        color4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color4);
            }
        });

        color5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color5);
            }
        });

        color6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color6);
            }
        });

        color7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color7);
            }
        });

        color8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color8);
            }
        });

        color9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color9);
            }
        });

        color10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color10);
            }
        });

        color11.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color11);
            }
        });

        color12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color12);
            }
        });

        color13.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color13);
            }
        });

        color14.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color14);
            }
        });

        color15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color15);
            }
        });

        color16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color16);
            }
        });

        color17.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color17);
            }
        });

        color18.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color18);
            }
        });

        color19.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color19);
            }
        });

        color20.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorClick(color20);
            }
        });

        builder.setView(view);
        this.dialog = builder.create();
        return dialog;
    }

    public void colorClick(ImageView imageView) {
        dialog.dismiss();
        if (getActivity() instanceof EventEditor) {
            ((EventEditor) getActivity()).setChooseColorCode("#"+Integer.toHexString(ImageViewCompat.getImageTintList(imageView).getDefaultColor()));
            ((ImageButton) getActivity().findViewById(R.id.color_chooser_dot))
                    .setColorFilter(ImageViewCompat.getImageTintList(imageView).getDefaultColor());
        } else if (getTargetFragment() instanceof DialogNewList) {
            ((DialogNewList)getTargetFragment()).selected_color_code = "#"+Integer.toHexString(ImageViewCompat.getImageTintList(imageView).getDefaultColor());
            TextView tv = ((TextView)((DialogNewList) getTargetFragment()).thisView.findViewById(R.id.new_list_colorPicker));
            tv.getCompoundDrawables()[0].setTint(ImageViewCompat.getImageTintList(imageView).getDefaultColor());
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("ticks", labels);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        this.labels = (HashMap) savedInstanceState.getSerializable("ticks");
    }
}
