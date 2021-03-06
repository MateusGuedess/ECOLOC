package br.com.fabappu9.ecoloc;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import br.com.fabappu9.ecoloc.DTO.PontoDto;
import br.com.fabappu9.ecoloc.network.APIClient;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.app.Activity.RESULT_OK;
import static br.com.fabappu9.ecoloc.MapaFragment.RetainedFragment.pontos;
import static br.com.fabappu9.ecoloc.interfaces.Constantes.CHAVE_PONTO_RECUPERADO;
import static br.com.fabappu9.ecoloc.interfaces.Constantes.TAG_ALTERAR_PONTO_CADASTRADO;
import static br.com.fabappu9.ecoloc.interfaces.Constantes.TAG_CODE_PERMISSION_LOCATION;
import static br.com.fabappu9.ecoloc.interfaces.Constantes.TAG_NOVO_PONTO_CADASTRADO;

/**
 * Created by Geraldo on 06/06/2017.
 * Fixed By Guilherme on 12/05/2018
 */

public class MapaFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener{


    private RetainedFragment mapWorkFragment;
    private MapView mMapView;
    private Marker mMarker;
    private String cadastrarEstePonto = "Deseja cadastrar este ponto?";
    private String cadastrarSnippet = "";

    private static GoogleMap mGoogleMap;

    private View mView;

    private static final String TAG = "MapaFragment";

    private SharedPreferences.Editor sharedPrefEditor;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        RetainedFragment fragment = (RetainedFragment) getFragmentManager().findFragmentByTag("work");

        if (fragment != null) {
            fragment.setTargetFragment(this, 0);
        }
    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_mapa, container, false);

        //LayoutInflater inflater = getActivity().getLayoutInflater();

        mostraMensagemComInformacoesNoPrimeiroLogin(inflater);

        mMapView = (MapView) mView.findViewById(R.id.map);
        if (mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }

        // --- cria fragment para salvar os pontos e camera ---
        FragmentManager fm = getFragmentManager();
        mapWorkFragment = (RetainedFragment)fm.findFragmentByTag("work");

        if (mapWorkFragment == null) {
            mapWorkFragment = new RetainedFragment();
            mapWorkFragment.setTargetFragment(this, 0);
            fm.beginTransaction().add(mapWorkFragment, "work").commit();
        }

        return mView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        mMapView.getMapAsync(this);
    }

    @Override
    public void onPause() {
        mMapView.onPause();
        super.onPause();
        mapWorkFragment.cameraGoogle = mGoogleMap.getCameraPosition();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @SuppressLint("InflateParams")
    private void mostraMensagemComInformacoesNoPrimeiroLogin(LayoutInflater inflater) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("jaLogouAntes", Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPreferences.edit();

        Boolean jaLogouAntes = sharedPreferences.getBoolean("jaLogouAntes", false);
        if(!jaLogouAntes){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Instuções")
                    .setView(inflater.inflate(R.layout.alert_dialog,null))
                    .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Boolean conferindo = sharedPrefEditor.putBoolean("jaLogouAntes", true).commit();
                        }
                    });
            builder.show();
        }
    }


    // --- Mapa ---
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        mGoogleMap = googleMap;

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==  PackageManager.PERMISSION_GRANTED &&  ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==  PackageManager.PERMISSION_GRANTED) {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    TAG_CODE_PERMISSION_LOCATION);
        }


        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().isZoomControlsEnabled();
        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);



        mapWorkFragment.configurarCallbackParaCarregarOsPontos(mGoogleMap);

        mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(-23.5489, -46.6388)).title("Minha ultima posição com sinal").snippet("Testando map fragment"));


        centralizarCamera(mGoogleMap);

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                String clickCount;
                clickCount = marker.getTitle();

                if (clickCount.equals(cadastrarEstePonto)) {
                    addPonto(marker.getPosition());
                } else{
                    abrirDetalhesPonto(marker);
                }
            }
        });

        mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                if (mMarker != null) {
                    mMarker.remove();
                    cadastrarSnippet = Localizador.encontrarEndereco(getActivity(), latLng.latitude, latLng.longitude);
                    mMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title(cadastrarEstePonto).snippet(cadastrarSnippet));
                } else {
                    cadastrarSnippet = Localizador.encontrarEndereco(getActivity(), latLng.latitude, latLng.longitude);
                    mMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latLng.latitude, latLng.longitude)).title(cadastrarEstePonto).snippet(cadastrarSnippet));
                    addPonto(latLng);
                }
            }
        });


    }


    private void addPonto(LatLng latLng){
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        Bundle params = new Bundle();
        params.putString("Endereco", Localizador.encontrarEndereco(getActivity(), latitude, longitude));
        params.putDouble("Latitude", latitude);
        params.putDouble("Longitude", longitude);
        Intent intent = new Intent(getActivity(), InfoEnderecoActivity.class);
        intent.putExtras(params);
        startActivityForResult(intent, TAG_NOVO_PONTO_CADASTRADO);
    }

    private void abrirDetalhesPonto(Marker marker) {
        String[] tituloParams = marker.getTitle().split("-");
        String idDoPonto = tituloParams[0];
        PontoDto ponto = pontos.get(Integer.parseInt(idDoPonto));

        Intent intent = new Intent(getActivity(), DetalhesEcoPontoActivity.class);
        intent.putExtra(CHAVE_PONTO_RECUPERADO, ponto);

        startActivityForResult(intent, TAG_ALTERAR_PONTO_CADASTRADO);

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            switch (requestCode){
                case TAG_NOVO_PONTO_CADASTRADO:
                    mMarker = null;
                    mGoogleMap.clear();
                    mapWorkFragment.iniCallback(mGoogleMap);
                    break;
                case TAG_ALTERAR_PONTO_CADASTRADO:
                    mMarker = null;
                    mGoogleMap.clear();
                    mapWorkFragment.iniCallback(mGoogleMap);
                    break;
            }
        }


        /*
        if(requestCode ==  TAG_NOVO_PONTO_CADASTRADO && resultCode == RESULT_OK){
            mMarker = null;
            mGoogleMap.clear();
            mapWorkFragment.iniCallback(mGoogleMap);
        }else{
            mMarker = null;
        }
        */

    }

    private void centralizarCamera(GoogleMap googleMap) {
        //LatLng coordenada = buscarLocalizacaoDoUsuario(mView.getContext()); FICOU NA MÃO, PERDAO PELO VACILO



        double lat_paulista = -23.559650;
        double long_paulista = -46.657941;

        LatLng coordenada = new LatLng(lat_paulista, long_paulista);

        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(coordenada, 15);
        googleMap.moveCamera(update);
    }

    @SuppressLint("MissingPermission")
    private LatLng buscarLocalizacaoDoUsuario(Context context){

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        assert locationManager != null;
        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        return new LatLng(latitude,longitude);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {  return false; }


    //--------------------------------------------------------------------------------------------------
    public static class RetainedFragment extends Fragment {
        protected CameraPosition cameraGoogle = null;
        public static List<PontoDto> pontos = null;
        Call<List<PontoDto>> retorno = null;


        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);

            cameraGoogle = CameraPosition.builder().target(new LatLng(-23.5489, -46.6388)).zoom(9).bearing(0).tilt(4).build();
        }


        void iniCallback(final GoogleMap mGoogleMap){
            retorno = new APIClient().getRestService().getPontoDTO("12345", "GETPONTOS", "");
            retorno.enqueue(new Callback<List<PontoDto>>() {
                @Override
                public void onResponse(@NonNull Call<List<PontoDto>> call, @NonNull Response<List<PontoDto>> response) {
                    pontos = response.body();
                    configurarCallbackParaCarregarOsPontos(mGoogleMap);
                }
                @Override
                public void onFailure(@NonNull Call<List<PontoDto>> call, @NonNull Throwable error) {
                }
            });
        }


        private void configurarCallbackParaCarregarOsPontos(GoogleMap mGoogleMap) {
            mGoogleMap.clear();

            if (pontos != null) {
                for (int i = 0; i < pontos.size(); i++) {
                    PontoDto dto = pontos.get(i);

                    String tituloPonto = i + "-" + dto.getDescricao();
                    LatLng posicaoPonto = new LatLng(Double.parseDouble(dto.getLatitude()), Double.parseDouble(dto.getLongitude()));
                    String tipoMaterialPonto = dto.getTipoMaterial();

                    MarkerOptions marcador = new MarkerOptions();
                    marcador.position(posicaoPonto);
                    marcador.title(tituloPonto);
                    marcador.snippet(tipoMaterialPonto);

                    mGoogleMap.addMarker(marcador);

                }
            }else
                iniCallback(mGoogleMap);
        }

    }
}