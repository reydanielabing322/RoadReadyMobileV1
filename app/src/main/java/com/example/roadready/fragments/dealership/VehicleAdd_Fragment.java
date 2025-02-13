package com.example.roadready.fragments.dealership;

import static com.example.roadready.classes.util.GetFileNameFromUri.getFileNameFromUri;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.roadready.R;
import com.example.roadready.classes.general.FileUtils;
import com.example.roadready.classes.general.ImagePicker;
import com.example.roadready.classes.general.MainFacade;
import com.example.roadready.classes.general.RoadReadyServer;
import com.example.roadready.classes.model.gson.ListingsDataGson;
import com.example.roadready.classes.model.gson.response.SuccessGson;
import com.example.roadready.databinding.FragmentDealershipVehicleAddBinding;

import java.io.File;

public class VehicleAdd_Fragment extends Fragment implements ImagePicker.OnImageSelectedListener{
	private final String TAG = "VehicleAdd_Fragment"; // declare TAG for each class for debugging purposes using Log.d()
	private FragmentDealershipVehicleAddBinding binding; // use View binding to avoid using too much findViewById
	private MainFacade mainFacade;
	private ImagePicker imagePicker;
	private Uri imageData;
	public View onCreateView(@NonNull LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {

		binding = FragmentDealershipVehicleAddBinding.inflate(inflater, container, false);
		View root = binding.getRoot();

		try {
			mainFacade = MainFacade.getInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		initImagePicker();

		return root;
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		initActions();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
	}

	private void initActions() {
		createSpinner();

		binding.addBtnBack.setOnClickListener(v -> mainFacade.getDealershipMyVehicleNavController().navigate(R.id.action_vehicleAdd_Fragment_to_myVehicles_Fragment));

		binding.addBtnUpload.setOnClickListener(v -> {
			imagePicker.selectImage(this);
		});


		binding.addBtnAddVehicle.setOnClickListener(v -> {

			showProgressBar();

			File listingImageFile = FileUtils.uriToFile(requireContext(), imageData);
			if(listingImageFile == null){
				mainFacade.makeToast("Add a vehicle image", Toast.LENGTH_SHORT);
				hideProgressBar();
				return;
			}
			String modelAndName = String.valueOf(binding.addInptModelName.getText());
			String make = String.valueOf(binding.addInptMake.getText());
			String fuelType = String.valueOf(binding.addInptFuelType.getText());
			String power = String.valueOf(binding.addInptPower.getText());
			String transmission = String.valueOf(binding.addInptTransmission.getText());
			String engine = String.valueOf(binding.addInptEngine.getText());
			String fuelTankCapacity = String.valueOf(binding.addInptFuelCap.getText());
			String seatingCapacity = String.valueOf(binding.addInptSeatingCap.getText());
			String price = String.valueOf(binding.addInptPrice.getText());
			String dealershipName = mainFacade.getSessionManager().getUserGson().getDealership().getName();
			String vehicleType = binding.addSpinnerVehicleType.getSelectedItem().toString();

			mainFacade.createListing(
				new RoadReadyServer.ResponseListener<ListingsDataGson>() {
					@Override
					public void onSuccess(SuccessGson<ListingsDataGson> response) {
						mainFacade.makeToast(response.getMessage(), Toast.LENGTH_SHORT);
						hideProgressBar();
					}
					@Override
					public void onFailure(int code, String message) {
						if (code != -1)
							mainFacade.makeToast(message, Toast.LENGTH_SHORT);
						hideProgressBar();
					}
				}, listingImageFile, modelAndName, make, fuelType, power, transmission, engine, fuelTankCapacity, seatingCapacity, price, dealershipName, vehicleType
			);
		});
	}

	private void createSpinner(){
		String[] items = new String[]{"Car", "Motor"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_dropdown_item, items);
		binding.addSpinnerVehicleType.setAdapter(adapter);
	}

	private void showProgressBar() {
		binding.addBtnAddVehicle.setEnabled(false);
		mainFacade.showBackDrop();
		mainFacade.showProgressBar();
	}

	private void hideProgressBar() {
		binding.addBtnAddVehicle.setEnabled(true);
		mainFacade.hideBackDrop();
		mainFacade.hideProgressBar();
	}

	private void initImagePicker() {
		imagePicker = new ImagePicker(mainFacade.getMainActivity().getActivityResultRegistry());
		getLifecycle().addObserver(imagePicker);
	}

	@Override
	public void onImageSelected(Uri uri) {
		if(uri != null) {
			binding.addInptUpload.setText(getFileNameFromUri(mainFacade.getMainActivity().getApplicationContext(), uri));
			imageData = uri;
		} else {
			mainFacade.makeToast("Image selection canceled", Toast.LENGTH_SHORT);
		}
	}
}