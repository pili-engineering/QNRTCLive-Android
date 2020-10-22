package com.qiniu.droid.rtc.live.demo.im.panel;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.qiniu.droid.rtc.live.demo.R;
import com.qiniu.droid.rtc.live.demo.im.DataInterface;
import com.qiniu.droid.rtc.live.demo.im.model.NeedLoginEvent;
import com.qiniu.droid.rtc.live.demo.im.utils.CommonUtils;

import de.greenrobot.event.EventBus;

public class BottomPanelFragment extends Fragment {

    private static final String TAG = "BottomPanelFragment";

    private ViewGroup buttonPanel;
    private ImageView btnInput;
    private InputPanel inputPanel;
    private GiftPanel giftPanel;
    private ImageView btnGift;
    private ImageView btnHeart;
    private ImageView btnBarrage;
    private BanListener banListener;
    private View mLlOptions;
    private View mIvSetting;

    private int softInputHeight;

    private GiftPanelListener mGiftPanelListener;

    public interface BanListener {
        void addBanWarn();
    }

    public interface GiftPanelListener {
        void onPanelOpen();

        void onPanelClose();
    }

    public void setBanListener(BanListener banListener) {
        this.banListener = banListener;
    }

    public void setGiftPanelListener(GiftPanelListener giftPanelListener) {
        mGiftPanelListener = giftPanelListener;
    }

    public void setBtnsVisible(int visible) {
        btnInput.setVisibility(visible);
        btnBarrage.setVisibility(visible);
        btnGift.setVisibility(visible);
        btnHeart.setVisibility(visible);
    }

    public boolean isPanelVisible() {
        boolean isInputPanelVisible = inputPanel.getVisibility() == View.VISIBLE;
        boolean isGiftPanelVisible = giftPanel.getVisibility() == View.VISIBLE;
        return isInputPanelVisible || isGiftPanelVisible;
    }

    public void showInputPanel() {
        if (isLoginAndCanInput()) {
            inputPanel.setVisibility(View.VISIBLE);
            isShowInputAboveKeyboard(true);
            inputPanel.setType(InputPanel.TYPE_TEXTMESSAGE);
        }
    }

    public void hidePanels() {
        if (inputPanel.getVisibility() == View.VISIBLE || giftPanel.getVisibility() == View.VISIBLE) {
            inputPanel.setVisibility(View.GONE);
            giftPanel.setVisibility(View.GONE);
            buttonPanel.setVisibility(View.VISIBLE);
            CommonUtils.hideInputMethod(getActivity(), inputPanel);

            if (mGiftPanelListener != null) {
                mGiftPanelListener.onPanelClose();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottombar, container);
        buttonPanel = (ViewGroup) view.findViewById(R.id.button_panel);
        btnInput = (ImageView) view.findViewById(R.id.btn_input);
        inputPanel = (InputPanel) view.findViewById(R.id.input_panel);
        giftPanel = (GiftPanel) view.findViewById(R.id.gift_panel);
        btnGift = (ImageView) view.findViewById(R.id.btn_gift);
        btnHeart = (ImageView) view.findViewById(R.id.btn_heart);
        btnBarrage = (ImageView) view.findViewById(R.id.btn_barrage);
        mLlOptions = view.findViewById(R.id.ll_options);
        mIvSetting = view.findViewById(R.id.iv_setting);

        giftPanel.setGiftSendListener(new GiftPanel.GiftSendListener() {
            @Override
            public void onGiftSent() {
                if (mGiftPanelListener != null) {
                    mGiftPanelListener.onPanelClose();
                }
            }
        });

        btnInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputPanel();
            }
        });

        btnBarrage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLoginAndCanInput()) {
                    inputPanel.setVisibility(View.VISIBLE);
                    inputPanel.setType(InputPanel.TYPE_BARRAGE);
                }
            }
        });

        btnGift.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isLogin()) {
                    giftPanel.setVisibility(View.VISIBLE);
                    if (mGiftPanelListener != null) {
                        mGiftPanelListener.onPanelOpen();
                    }
                }
            }
        });

        return view;
    }

    public boolean isLogin() {
        if (DataInterface.isLogin()) {
            return true;
        } else {
            EventBus.getDefault().post(new NeedLoginEvent(true));
            return false;
        }
    }

    public boolean isLoginAndCanInput() {
        if (DataInterface.isLogin()) {
            if (DataInterface.isBanStatus()) {
                if (banListener != null) {
                    banListener.addBanWarn();
                }
                return false;
            }
            return true;
        } else {
            EventBus.getDefault().post(new NeedLoginEvent(true));
            return false;
        }
    }

    public void setOptionViewIsDisplay(boolean isDisplay) {
        mLlOptions.setVisibility(isDisplay ? View.VISIBLE : View.INVISIBLE);
        mIvSetting.setVisibility(isDisplay ? View.INVISIBLE : View.INVISIBLE);
    }

    public boolean isInputPanelVisible() {
        return inputPanel.getVisibility() == View.VISIBLE;
    }

    /**
     * back键或者空白区域点击事件处理
     *
     * @return 已处理true, 否则false
     */
    public boolean onBackAction() {
        if (inputPanel.onBackAction()) {
            return true;
        }
        if (inputPanel.getVisibility() == View.VISIBLE || giftPanel.getVisibility() == View.VISIBLE) {
            inputPanel.setVisibility(View.GONE);
            giftPanel.setVisibility(View.GONE);
            buttonPanel.setVisibility(View.VISIBLE);
            CommonUtils.hideInputMethod(getActivity(), inputPanel);

            if (mGiftPanelListener != null) {
                mGiftPanelListener.onPanelClose();
            }
            return true;
        }
        return false;
    }

    public void setInputPanelListener(InputPanel.InputPanelListener l) {
        inputPanel.setPanelListener(l);
    }

    public void setSoftInputHeight(int inputHeight) {
        this.softInputHeight = inputHeight;
    }

    public void isShowInputAboveKeyboard(boolean isAboveKeyboard) {
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) inputPanel.getLayoutParams();
        if (isAboveKeyboard) {
            layoutParams.bottomMargin = softInputHeight;
        } else {
            layoutParams.bottomMargin = 0;
        }
        inputPanel.setLayoutParams(layoutParams);
    }
}
