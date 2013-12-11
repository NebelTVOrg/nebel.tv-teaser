package com.nebel_tv.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

import com.nebel_tv.R;
import com.nebel_tv.storage.LocalStorage;
import com.nebel_tv.ui.fragment.base.BaseWebViewClient;
import com.nebel_tv.ui.fragment.base.WebViewUILoaderHelper;
import com.nebel_tv.utils.DownloadManagerHelper;

public class PrivacyDialogFragment extends DialogFragment {
	
	private static final String TAG = PrivacyDialogFragment.class.getName();
	private static final String POLICY_URL = "http://nebel.tv";
	
	public static void showPrivacyDialog(FragmentManager fm) {
		PrivacyDialogFragment privacyDialog = new PrivacyDialogFragment();
		privacyDialog.setCancelable(false);
		
		privacyDialog.show(fm, TAG);
	}
	
	private WebView policyWebView;
	private ProgressBar progressBar;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = getActivity().getLayoutInflater();

        builder
        	.setTitle(R.string.policy_title)
        	.setView(inflater.inflate(R.layout.fragment_webview, null))
            .setPositiveButton(R.string.policy_btn_accept, new DialogInterface.OnClickListener() {
            	@Override
                public void onClick(final DialogInterface dialog, final int id) {
                     LocalStorage.from(getActivity()).setPolicyAccepted();
                     DownloadManagerHelper.startVideoDownload(getActivity());
                     dismiss();
                }
            })
            .setNegativeButton(R.string.policy_btn_decline, new DialogInterface.OnClickListener() {
            	@Override
                public void onClick(final DialogInterface dialog, final int id) {
            		 dismiss();
                     getActivity().finish();
                }
            });

        return builder.create();
	}
	
	@Override
	public void onStart() {
		super.onStart();
		progressBar = (ProgressBar) getDialog().findViewById(R.id.progress_bar);
		policyWebView = (WebView) getDialog().findViewById(R.id.webview);
		policyWebView.getSettings().setJavaScriptEnabled(true);
		WebViewUILoaderHelper helper = new WebViewUILoaderHelper(policyWebView, progressBar);
		policyWebView.setWebViewClient(new BaseWebViewClient(helper));
		policyWebView.setWebChromeClient(new WebChromeClient());
		
		policyWebView.loadUrl(POLICY_URL);
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		policyWebView.stopLoading();
		super.onDismiss(dialog);
	}

}
