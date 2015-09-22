package io.flic.demo.app.flicboilerplate;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

public class MainActivity extends AppCompatActivity {
	private static final String TAG = "MainActivity";
	private FlicManager manager;

	private FlicButtonCallback buttonCallback = new FlicButtonCallback() {
		@Override
		public void onButtonUpOrDown(FlicButton button, boolean wasQueued, int timeDiff, boolean isUp, boolean isDown) {
			final String text = button + " was " + (isDown ? "pressed" : "released");
			Log.d(TAG, text);

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					TextView tv = (TextView) findViewById(R.id.textView);
					tv.setText(text);
				}
			});
		}
	};

	private void setButtonCallback(FlicButton button) {
		button.removeAllFlicButtonCallbacks();
		button.addFlicButtonCallback(buttonCallback);
		button.setFlicButtonCallbackFlags(FlicButtonCallbackFlags.UP_OR_DOWN);
		button.setActiveMode(true);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		FlicManager.setAppCredentials("[appId]", "[appSecret]");
		FlicManager.getInstance(this, new FlicManagerInitializedCallback() {

			@Override
			public void onInitialized(FlicManager manager) {
				Log.d(TAG, "Ready to use manager");

				MainActivity.this.manager = manager;

				// Restore buttons grabbed in a previous run of the activity
				List<FlicButton> buttons = manager.getKnownButtons();
				for (FlicButton button : buttons) {
					String status = null;
					switch (button.getConnectionStatus()) {
						case FlicButton.BUTTON_DISCONNECTED:
							status = "disconnected";
							break;
						case FlicButton.BUTTON_CONNECTION_STARTED:
							status = "connection started";
							break;
						case FlicButton.BUTTON_CONNECTION_COMPLETED:
							status = "connection completed";
							break;
					}
					Log.d(TAG, "Found an existing button: " + button + ", status: " + status);
					setButtonCallback(button);
				}
			}
		});
	}

	@Override
	protected void onDestroy() {
		FlicManager.destroyInstance();
		super.onDestroy();
	}

	public void grabButton(View v) {
		if (manager != null) {
			manager.initiateGrabButton(this);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		FlicButton button = manager.completeGrabButton(requestCode, resultCode, data);
		if (button != null) {
			Log.d(TAG, "Got a button: " + button);
			setButtonCallback(button);
		}
	}
}
