/**
 * Copyright (C) 2014 Nebel TV (http://nebel.tv)
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
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
import com.nebel_tv.frontend.FrontendUpdateTask;
import com.nebel_tv.storage.LocalStorage;
import com.nebel_tv.ui.fragment.base.BaseWebViewClient;
import com.nebel_tv.ui.fragment.base.WebViewUILoaderHelper;
import com.nebel_tv.utils.DownloadManagerHelper;

public class PrivacyDialogFragment extends DialogFragment {

	private static final String TAG = PrivacyDialogFragment.class.getName();
	private static final String POLICY_URL = "file:///android_asset/privacy_policy.html";

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

		builder.setTitle(R.string.policy_title).setView(inflater.inflate(R.layout.fragment_webview, null))
				.setPositiveButton(R.string.policy_btn_accept, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int id) {
						LocalStorage.from(getActivity()).setPolicyAccepted();
						// TODO temp disable video files loading
						// DownloadManagerHelper.startVideoDownload(getActivity());
						new FrontendUpdateTask(getActivity()).execute();
						dismiss();
					}
				}).setNegativeButton(R.string.policy_btn_decline, new DialogInterface.OnClickListener() {
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
