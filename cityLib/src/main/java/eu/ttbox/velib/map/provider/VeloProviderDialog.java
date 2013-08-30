package eu.ttbox.velib.map.provider;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import eu.ttbox.velib.R;
import eu.ttbox.velib.model.Station;
import eu.ttbox.velib.model.VelibProvider;
import eu.ttbox.velib.service.VelibService;

public class VeloProviderDialog extends Dialog {

    private  Context context;

	public VeloProviderDialog(final Context context, final VelibProvider velibProvider, final VelibService velibService) {
		super(context);
        this.context = context;
		setContentView(R.layout.velo_provider_dialog);
		// Delete Button
		Button deleteButton = (Button) findViewById(R.id.deleteButton);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				velibService.removeAllStationsByProvider(velibProvider);
			}
		});
		// Update Button
		Button updateButton = (Button) findViewById(R.id.updateButton);
		updateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//                new DownloadAsync(context, velibService).execute(velibProvider);
                ArrayList<Station> stations = velibService.getStationsByProviderWithCheckUpdateDate(velibProvider, true);
                Toast.makeText(context, "Download Stations size " + stations.size(), Toast.LENGTH_SHORT).show();
			}
		});

	}


    static class DownloadAsync extends AsyncTask<VelibProvider, Void, ArrayList<Station>> {

        Context context;
        VelibService velibService;

        DownloadAsync(Context context, VelibService velibService) {
            this.context = context;
            this.velibService = velibService;
        }

        @Override
        protected ArrayList<Station> doInBackground(VelibProvider... params) {
            if (params!=null && params.length>0) {
                for (VelibProvider velibProvider : params ) {
                    ArrayList<Station> stations = velibService.getStationsByProviderWithCheckUpdateDate(velibProvider, true);
                    Toast.makeText(context, "Download Stations size " + stations.size(), Toast.LENGTH_SHORT).show();
                }
            }
            return null;
        }
    };

}
