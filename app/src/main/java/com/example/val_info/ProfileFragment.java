package com.example.val_info;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.core.app.ShareCompat;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.val_info.database.InternalPreference;

public class ProfileFragment extends Fragment {

    private final int KEY_ACTION = 69;
    private InternalPreference mPref;
    private ImageView mProfile, mPicture;
    StringBuilder txt = new StringBuilder();

    public ProfileFragment(InternalPreference pref) {
        this.mPref = pref;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View parent = inflater.inflate(R.layout.fragment_profile, container, false);

        InternalPreference pref = new InternalPreference(parent.getContext());
        CardView mRoundProfile = parent.findViewById(R.id.fp_cv_round_picture);
        mPicture = parent.findViewById(R.id.fp_iv_profile_picture);
        mProfile = parent.findViewById(R.id.fp_image_profile);
        TextView tv_fullname = parent.findViewById(R.id.fp_text_fullName);
        TextView tv_email = parent.findViewById(R.id.fp_text_email);
        TextView tv_division = parent.findViewById(R.id.fp_text_division);
        TextView tv_phonenumber = parent.findViewById(R.id.fp_text_phoneNumber);
        TextView tv_status = parent.findViewById(R.id.fp_text_status);
        TextView tv_nip = parent.findViewById(R.id.fp_text_nip_profile);
        TextView tutorialTextView = parent.findViewById(R.id.fp_text_tutorial);
        TextView changePasswordTextView = parent.findViewById(R.id.fp_text_changePassword); // Ensure this is correctly referenced

        if (!mPref.ADMIN) {
            tv_fullname.setText(mPref.USER);
            tv_email.setText(mPref.EMAIL);
            tv_division.setText(mPref.DIVISI == 1 ? "DBS" : "Maybank");
            tv_phonenumber.setText("+62" + mPref.PHONE);
            tv_status.setText("Mitra " + mPref.STATUS);
            tv_nip.setText(mPref.NIP);

            // Set profile
            if (!mPref.getImageProfile().equals(mPref.DEF_PROFILE)) {
                byte[] b = Base64.decode(mPref.getImageProfile(), Base64.DEFAULT);
                Bitmap bp = BitmapFactory.decodeByteArray(b, 0, b.length);
                mPicture.setImageDrawable(new BitmapDrawable(parent.getResources(), bp));
            }

            mProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent open = new Intent();
                    open.setType("image/*");
                    open.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(open, "Pick Picture for Profile"), KEY_ACTION);
                }
            });
        } else {
            tv_fullname.setText("Admin");
            tv_email.setText("Admin");
            tv_division.setText("Admin");
            tv_phonenumber.setText("Admin");
            tv_status.setText("Admin");
            tv_nip.setText("Admin");
        }

        tutorialTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTutorialDialog(parent.getContext());
            }
        });

        changePasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), RegisterFragment.class);
                intent.putExtra("ACTION", "CHANGE_PASSWORD");
                startActivity(intent);
            }
        });

        parent.findViewById(R.id.fp_text_contact).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mDial = mPref.getAlertDialog(parent.getContext());
                mDial.setTitle("Contact")
                        .setMessage("PT. Valdo International\nEmail : info@valdoinc.com\nPhone : +6221-2355-7599")
                        .setCancelable(true)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                AlertDialog alert = mDial.create();
                alert.show();
            }
        });

        parent.findViewById(R.id.fp_text_changeData).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendRequest(parent.getContext());
            }
        });

        parent.findViewById(R.id.fr_profile_btn_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pref.saveLoginStatus(false);
                Global.TO_HOME = true;
                startActivity(new Intent(parent.getContext(), MainActivity.class));
                getActivity().finish();
            }
        });

        return parent;
    }

    private void sendRequest(Context c) {
        Dialog dial = new Dialog(c);
        dial.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dial.setContentView(R.layout.request_dialog);
        dial.setCancelable(true);

        dial.findViewById(R.id.rd_button_submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (((CheckBox) dial.findViewById(R.id.rd_user)).isChecked())
                    txt.append("Change user\n");
                if (((CheckBox) dial.findViewById(R.id.rd_email)).isChecked())
                    txt.append("Change email\n");
                /*if (((CheckBox) dial.findViewById(R.id.rd_password)).isChecked())
                    txt.append("Change password\n");*/
                if (((CheckBox) dial.findViewById(R.id.rd_divisi)).isChecked())
                    txt.append("Change divisi\n");
                if (((CheckBox) dial.findViewById(R.id.rd_mitra)).isChecked())
                    txt.append("Change mitra\n");

                if (txt.toString().length() > 0) {
                    ShareCompat.IntentBuilder.from(getActivity())
                            .setType("message/rfc822")
                            .addEmailTo("nalnalfill5@gmail.com")
                            .setSubject("Request Changes Data")
                            .setText("User : " + mPref.USER + "\nNIP : " + mPref.NIP + "\nEmail : " + mPref.EMAIL + "\n\nRequest : \n" + txt.toString())
                            .setChooserTitle("Request Changes Data")
                            .startChooser();
                    dial.cancel();
                } else
                    Toast.makeText(dial.getContext(), "Please choose your data want to update!", Toast.LENGTH_LONG).show();
            }
        });

        WindowManager.LayoutParams wm = new WindowManager.LayoutParams();
        wm.copyFrom(dial.getWindow().getAttributes());
        wm.width = (int) (getWidth(c) * 0.9f);
        wm.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dial.show();
        dial.getWindow().setAttributes(wm);
    }

    private void showTutorialDialog(Context context) {
        Dialog tutorialDialog = new Dialog(context);
        tutorialDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        tutorialDialog.setContentView(R.layout.tutorial_de);
        tutorialDialog.setCancelable(true);

        Button closeButton = tutorialDialog.findViewById(R.id.btn_close_tutorial);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tutorialDialog.dismiss();
            }
        });

        WindowManager.LayoutParams wm = new WindowManager.LayoutParams();
        wm.copyFrom(tutorialDialog.getWindow().getAttributes());
        wm.width = (int) (getWidth(context) * 0.9f);
        wm.height = WindowManager.LayoutParams.WRAP_CONTENT;
        tutorialDialog.show();
        tutorialDialog.getWindow().setAttributes(wm);
    }

    private int getWidth(Context c) {
        return c.getResources().getDisplayMetrics().widthPixels;
    }
}

