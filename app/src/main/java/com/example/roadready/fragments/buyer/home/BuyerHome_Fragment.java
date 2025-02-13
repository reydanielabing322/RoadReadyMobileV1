package com.example.roadready.fragments.buyer.home;

import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.roadready.R;
import com.example.roadready.classes.general.MainFacade;
import com.example.roadready.classes.general.RoadReadyServer;
import com.example.roadready.classes.model.gson.DealershipsDataGson;
import com.example.roadready.classes.model.gson.ListingsDataGson;
import com.example.roadready.classes.model.gson.data.DealershipGson;
import com.example.roadready.classes.model.gson.data.VehicleGson;
import com.example.roadready.classes.model.gson.response.SuccessGson;
import com.example.roadready.classes.ui.adapter.BuyerDealershipListRecyclerViewAdapter;
import com.example.roadready.classes.ui.adapter.BuyerHotListingsRecyclerViewAdapter;
import com.example.roadready.classes.ui.adapter.BuyerVehicleListingsRecyclerViewAdapter;
import com.example.roadready.databinding.FragmentBuyerHomeBinding;

import java.util.ArrayList;

import java.util.List;

public class BuyerHome_Fragment extends Fragment {

    private enum ItemListingFilters {
        VEHICLE, DEALERSHIP,
    }
    private final String TAG = "BuyerHome_Fragment";
    private FragmentBuyerHomeBinding binding;
    private MainFacade mainFacade;
    private List<VehicleGson> listingList;
    private List<VehicleGson> activeListingList;
    private List<DealershipGson> dealershipList;
    private List<DealershipGson> activeDealershipList;
    boolean isListingSelected = true; // true = vehicle, false = dealership
    private Location currentLocation;
    private final int HOT_VEHICLE_PRICE_THRESHOLD = 100000;
    private boolean isSpinnerVisible = false;
    private int listingCount;
    private int dealershipCount;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBuyerHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        listingList = new ArrayList<>();
        activeListingList = new ArrayList<>();
        dealershipList = new ArrayList<>();
        activeDealershipList = new ArrayList<>();
        listingCount = 0;
        dealershipCount = 0;

        try {
            mainFacade = MainFacade.getInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        mainFacade.hideProgressBar();
        mainFacade.hideBackDrop();

        mainFacade.fetchLocation(location -> currentLocation = location);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainFacade.showProgressBar();
        binding.getRoot().setVisibility(View.INVISIBLE);

        final RoadReadyServer.ResponseListener<DealershipsDataGson> dealershipResponseListener = new RoadReadyServer.ResponseListener<DealershipsDataGson>() {
            @Override
            public void onSuccess(SuccessGson<DealershipsDataGson> response) {
                dealershipList.addAll(response.getData().getDealerships());
                updateDealershipScrollViewItems(dealershipList);
                binding.bhSVDealership.setLayoutManager(new LinearLayoutManager(mainFacade.getMainActivity().getApplicationContext()));
                dealershipCount = response.getData().getDealerships().size();
                setDealershipCount();
            }

            @Override
            public void onFailure(int code, String message) {
                if (code != -1)
                    mainFacade.makeToast(message, Toast.LENGTH_SHORT);
                setDealershipCount();
            }
        };
        mainFacade.getDealerships(dealershipResponseListener, null, null);

        final RoadReadyServer.ResponseListener<ListingsDataGson> listingResponseListener = new RoadReadyServer.ResponseListener<ListingsDataGson>() {
            @Override
            public void onSuccess(SuccessGson<ListingsDataGson> response) {
                for(VehicleGson vehicleGson : response.getData().getListings()) {
                    if(vehicleGson.isAvailable()) {
                        listingList.add(vehicleGson);
                    }
                }
                initHotListings();
                initSearchBar();
                updateListingScrollViewItems(listingList);
                binding.bhSVItems.setLayoutManager(new LinearLayoutManager(mainFacade.getMainActivity().getApplicationContext()));
                initFilterButton();
                if (binding != null)
                    binding.getRoot().setVisibility(View.VISIBLE);
                listingCount = response.getData().getListings().size();
                setListingCount();
                mainFacade.hideProgressBar();
            }

            @Override
            public void onFailure(int code, String message) {
                if (code != -1)
                    mainFacade.makeToast(message, Toast.LENGTH_SHORT);
                setListingCount();
                mainFacade.hideProgressBar();
                if (binding != null)
                    binding.getRoot().setVisibility(View.VISIBLE);
            }
        };

        mainFacade.getListings(listingResponseListener, null, null, null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initHotListings() {
        List<VehicleGson> filteredList = new ArrayList<>();
        for (VehicleGson item : listingList) {
            if (item.getPrice() < HOT_VEHICLE_PRICE_THRESHOLD) {
                filteredList.add(item);
            }
        }

        binding.bhRVRecommended.setAdapter(new BuyerHotListingsRecyclerViewAdapter(
                mainFacade.getMainActivity().getApplicationContext(),
                filteredList, itemId -> {
            BuyerHome_FragmentDirections.ActionBuyerHomepageFragmentToSelectingCarFragment action =
                    BuyerHome_FragmentDirections.actionBuyerHomepageFragmentToSelectingCarFragment();
            action.setModelId(itemId);
            mainFacade.getBuyerHomeNavController().navigate(action);
        }
        ));
    }

    private void initSearchBar() {
        binding.bhInptSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateItemListings(s);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void initFilterButton() {
        binding.bhBtnFilterSearch.setOnClickListener(v -> {
            isSpinnerVisible = !isSpinnerVisible;
            toggleSpinnerFilter();
        });
    }

    private void updateListingScrollViewItems(List<VehicleGson> data) {
        binding.bhSVItems.setAdapter(new BuyerVehicleListingsRecyclerViewAdapter(
                mainFacade.getMainActivity().getApplicationContext(),
                currentLocation,
                data, itemId -> {
            BuyerHome_FragmentDirections.ActionBuyerHomepageFragmentToSelectingCarFragment action =
                                    BuyerHome_FragmentDirections.actionBuyerHomepageFragmentToSelectingCarFragment();
                            action.setModelId(itemId);
                            mainFacade.getBuyerHomeNavController().navigate(action);
            }
        ));
    }

    private void updateDealershipScrollViewItems(List<DealershipGson> data){
        binding.bhSVDealership.setAdapter(new BuyerDealershipListRecyclerViewAdapter(
                mainFacade.getMainActivity().getApplicationContext(),
                data, itemId -> {
            BuyerHome_FragmentDirections.ActionBuyerHomepageFragmentToSelectingDealershipFragment action =
                    BuyerHome_FragmentDirections.actionBuyerHomepageFragmentToSelectingDealershipFragment();
            action.setDealershipId(itemId);
            mainFacade.getBuyerHomeNavController().navigate(action);
        }
        ));
    }

    private void toggleSpinnerFilter() {
        binding.bhSpinnerFilter.setVisibility(isSpinnerVisible ? View.INVISIBLE : View.VISIBLE);

        String[] filterOptions = {"Vehicle", "Dealership"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(mainFacade.getMainActivity().getApplicationContext(),
                android.R.layout.simple_spinner_item, filterOptions);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        binding.bhSpinnerFilter.setAdapter(adapter);
        binding.bhSpinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedFilter = (String) parent.getItemAtPosition(position);
                ItemListingFilters filter = null;
                switch (selectedFilter) {
                    case "Vehicle":
                        binding.bhInptSearch.setHint(R.string.text_search_vehicle);
                        binding.bhSVDealership.setVisibility(View.GONE);
                        binding.bhSVItems.setVisibility(View.VISIBLE);
                        isListingSelected = true;
                        updateListingScrollViewItems(listingList);
                        filter = ItemListingFilters.VEHICLE;
                        break;
                    case "Dealership":
                        binding.bhInptSearch.setHint(R.string.text_search_dealership);
                        binding.bhSVDealership.setVisibility(View.VISIBLE);
                        binding.bhSVItems.setVisibility(View.GONE);
                        isListingSelected = false;
                        updateDealershipScrollViewItems(dealershipList);
                        filter = ItemListingFilters.DEALERSHIP;
                        break;
                    default:
                        break;
                }
//                if (filter != null) {
//                    sortItemListings(filter);
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }


    private void updateItemListings(CharSequence s) {
        activeListingList.clear();
        activeDealershipList.clear();

        if(isListingSelected){
            for(VehicleGson items : listingList) {
                if(items.getModelAndName().toLowerCase().contains(s.toString().toLowerCase())) {
                    activeListingList.add(items);
                }
                if(activeListingList.isEmpty()){
                    binding.bhTxtSearchCount.setVisibility(View.VISIBLE);
                }else{
                    binding.bhTxtSearchCount.setVisibility(View.GONE);
                }
            }
            updateListingScrollViewItems(activeListingList);
        }else{
            for(DealershipGson items : dealershipList) {
                if(items.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                    activeDealershipList.add(items);
                }
                if(activeDealershipList.isEmpty()){
                    binding.bhTxtSearchCount.setVisibility(View.VISIBLE);
                }else{
                    binding.bhTxtSearchCount.setVisibility(View.GONE);
                }
            }
            updateDealershipScrollViewItems(activeDealershipList);
        }
    }

    public void setListingCount(){
        if (binding != null) {
            if(listingCount <= 0){
                binding.bhTxtListingCount.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setDealershipCount(){
        if (binding != null) {
            if(dealershipCount <= 0){
                binding.bhTxtDealershipCount.setVisibility(View.VISIBLE);
            }
        }
    }
}