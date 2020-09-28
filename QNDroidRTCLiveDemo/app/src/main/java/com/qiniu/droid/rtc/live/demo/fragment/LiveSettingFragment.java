package com.qiniu.droid.rtc.live.demo.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import com.qiniu.droid.rtc.live.demo.R;

public class LiveSettingFragment extends BaseBottomSheetFragment {
    public static final String TAG = "LiveSettingFragment";

    private RadioGroup mMicrophoneSettingGroup;
    private RadioGroup mSpeakerSettingGroup;
    private RadioGroup mFlashlightSettingGroup;

    private OnLiveSettingClickListener mOnLiveSettingClickListener;

    public interface OnLiveSettingClickListener {
        void onFragmentResumed();
        void onMicrophoneSettingChanged(boolean isOn);
        void onSpeakerSettingChanged(boolean isOn);
        void onFlashlightSettingChanged(boolean isOn);
    }

    public void setOnLiveSettingClickListener(OnLiveSettingClickListener listener) {
        mOnLiveSettingClickListener = listener;
    }

    public void setFlashlightSettingEnabled(boolean enabled) {
        if (mFlashlightSettingGroup != null) {
            setRadioGroupEnabled(mFlashlightSettingGroup, enabled);
        }
    }

    public void resetFlashlightSetting() {
        if (mFlashlightSettingGroup != null) {
            mFlashlightSettingGroup.check(R.id.flashlight_off);
        }
    }

    private void setRadioGroupEnabled(RadioGroup radioGroup, boolean enabled) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(enabled);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.bottom_sheet_live_settings;
    }

    @Override
    protected void initViewAndEvents(View view, Bundle savedInstanceState) {
        mMicrophoneSettingGroup = view.findViewById(R.id.microphone_radio_group);
        mSpeakerSettingGroup = view.findViewById(R.id.speaker_radio_group);
        mFlashlightSettingGroup = view.findViewById(R.id.flashlight_radio_group);
        mMicrophoneSettingGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mOnLiveSettingClickListener != null) {
                mOnLiveSettingClickListener.onMicrophoneSettingChanged(checkedId == R.id.microphone_on);
            }
        });
        mSpeakerSettingGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mOnLiveSettingClickListener != null) {
                mOnLiveSettingClickListener.onSpeakerSettingChanged(checkedId == R.id.speaker_on);
            }
        });
        mFlashlightSettingGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (mOnLiveSettingClickListener != null) {
                mOnLiveSettingClickListener.onFlashlightSettingChanged(checkedId == R.id.flashlight_on);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOnLiveSettingClickListener != null) {
            mOnLiveSettingClickListener.onFragmentResumed();
        }
    }
}
