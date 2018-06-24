package br.com.fabappu9.ecoloc;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

import br.com.fabappu9.ecoloc.DTO.MaterialDto;
import br.com.fabappu9.ecoloc.Model.RespostaPonto;
import br.com.fabappu9.ecoloc.adapter.TiposMaterialAdapter;
import br.com.fabappu9.ecoloc.network.APIClient;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InfoEnderecoActivity extends AppCompatActivity {

    private static final String TAG = "InfoEnderecoActivity";
    EditText endereco;
    ListView listTipoMaterial;
    EditText nome;
    Double latitude, longitude;
    private Button btnCadastrarPonto;
    private SpotsDialog dialog;
    private List<MaterialDto> materiais = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_endereco);

        iniciarComponentesTela();

        carregarParametrosRecebidos();

        btnCadastrarPonto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrarPontoNovo();
            }
        });
    }

    private void cadastrarPontoNovo() {
        boolean existeCamposFaltandoPreenchimento = getCheckedItemCount() == 0 || nome.getText().toString().equals("");
        if (existeCamposFaltandoPreenchimento) {
            Toast.makeText(InfoEnderecoActivity.this, "Preencha todos os campos ", Toast.LENGTH_SHORT).show();
        } else {
            dispararAPIcadastroPontoNovo();
        }
    }

    private void dispararAPIcadastroPontoNovo() {
        Call<RespostaPonto> resposta;
        dialog.show();
        SharedPreferences sharedPreferences = getSharedPreferences("id", Context.MODE_PRIVATE);
        String id = sharedPreferences.getString("id", "8");
        resposta = new APIClient().getRestService().setPontoDTO("12345",
                "CRIARPONTO",
                nome.getText().toString(),
                getIdsTems(),
                latitude.toString(),
                longitude.toString(),
                id
        );
        configurarCallbackEnviaPonto(resposta);
    }

    private void iniciarComponentesTela() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        dialog = new SpotsDialog(this, R.style.estilo_carregando);
        nome = (EditText) findViewById(R.id.edtNomePonto);
        endereco = (EditText) findViewById(R.id.editTxtEndereco);
        listTipoMaterial = (ListView) findViewById(R.id.list_checkBox);
        btnCadastrarPonto = (Button) findViewById(R.id.btnConfirmar);

        configurarCallbackListaMateriais();
    }

    private void carregarParametrosRecebidos() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle params = intent.getExtras();
            if (params != null) {
                String enderecoSaida = params.getString("Endereco");
                latitude = params.getDouble("Latitude");
                longitude = params.getDouble("Longitude");
                endereco.setText(enderecoSaida);
            }
        }
    }

    private int getCheckedItemCount() {
        int count = 0;
        for (MaterialDto m : materiais)
            count += m.isMarcado() ? 1 : 0;
        return count;
    }

    private String getIdsTems() {
        char separador = ',';
        StringBuilder ids = new StringBuilder();
        for (MaterialDto m : materiais) {
            if (m.isMarcado())
                ids.append(m.getId()).append(separador);
        }
        return ids.toString();
    }

    private void initTipoMaterial(List<MaterialDto> materiais) {
        listTipoMaterial.setAdapter(new TiposMaterialAdapter(this,  R.layout.list_item_checkbox, materiais));
    }



    private void configurarCallbackListaMateriais() {
        Call<List<MaterialDto>> retorno = new APIClient().getRestService().getMaterialDTO("12345", "GETTIPOMATERIAL", "");
        retorno.enqueue(new Callback<List<MaterialDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<MaterialDto>> call, @NonNull Response<List<MaterialDto>> response) {
                if (!response.isSuccessful()) {
                    Log.e("ERRO:", response.message());
                } else {
                    materiais = response.body();
                    initTipoMaterial(materiais);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MaterialDto>> call, @NonNull Throwable error) {
                Toast.makeText(InfoEnderecoActivity.this, "Erro: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    
    private void configurarCallbackEnviaPonto(Call<RespostaPonto> resposta) {
        resposta.enqueue(new Callback<RespostaPonto>() {
            @Override
            public void onResponse(@NonNull Call<RespostaPonto> call, @NonNull Response<RespostaPonto> response) {
                dialog.hide();
                setResult(RESULT_OK);
                mostrarMensagemAgradecimento();
            }

            @Override
            public void onFailure(@NonNull Call<RespostaPonto> call, @NonNull Throwable error) {
                dialog.hide();
                Toast.makeText(InfoEnderecoActivity.this, "Algum erro aconteceu: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        setResult(RESULT_CANCELED);
        finish();
        return super.onOptionsItemSelected(item);
    }


    private void mostrarMensagemAgradecimento() {
        LayoutInflater li = getLayoutInflater();
        @SuppressLint("InflateParams") View view = li.inflate(R.layout.alert_agradecimento, null);
        // btn ok
        view.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                finish();
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(InfoEnderecoActivity.this);
        builder.setView(view);
        builder.create().show();
    }

}

