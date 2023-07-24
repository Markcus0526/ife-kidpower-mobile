package org.unicefkidpower.kid_power;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

public class MainActivity extends AppCompatActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		connectGoogleFit();

		Button btn = (Button)findViewById(R.id.button);
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onClickedTest();
			}
		});
	}

	private void onClickedTest() {
	}

	public void connectGoogleFit() {
		GoogleApiClient apiClient = new GoogleApiClient.Builder(MainActivity.this)
				.addApi(Fitness.RECORDING_API)
				.addApi(Fitness.HISTORY_API)
				.addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
				.addConnectionCallbacks(
						new GoogleApiClient.ConnectionCallbacks() {

							@Override
							public void onConnected(Bundle bundle) {
								Toast.makeText(MainActivity.this, "Connection success", Toast.LENGTH_LONG).show();
							}

							@Override
							public void onConnectionSuspended(int i) {
								// If your connection to the sensor gets lost at some point,
								// you'll be able to determine the reason and react to it here.
								if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
									Toast.makeText(MainActivity.this, "Connection lost.  Cause: Network Lost.", Toast.LENGTH_LONG).show();
								} else if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
									Toast.makeText(MainActivity.this, "Connection lost.  Reason: Service Disconnected", Toast.LENGTH_LONG).show();
								}
							}
						}
				)
				.addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
						Toast.makeText(MainActivity.this,
								"Connection failed : " + connectionResult.getErrorCode() + " : " + connectionResult.getErrorMessage(),
								Toast.LENGTH_LONG)
								.show();
					}
				})
				.enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
					@Override
					public void onConnectionFailed(ConnectionResult result) {
						Toast.makeText(MainActivity.this,
								"Connection failed (enableAutoManage): " + result.getErrorCode() + " : " + result.getErrorMessage(),
								Toast.LENGTH_LONG)
								.show();
					}
				})
				.build();
	}


//	@Override
//	public void onActivityResult(int requestCode, int resultCode, Intent data) {
//		super.onActivityResult(requestCode, resultCode, data);
//		Toast.makeText(MainActivity.this, "requestCode : " + requestCode + ", resultCode : " + resultCode, Toast.LENGTH_LONG).show();
//	}

}
