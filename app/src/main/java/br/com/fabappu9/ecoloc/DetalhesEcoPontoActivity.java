package br.com.fabappu9.ecoloc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import br.com.fabappu9.ecoloc.DTO.PontoDto;
import br.com.fabappu9.ecoloc.Model.Resposta;
import br.com.fabappu9.ecoloc.Model.RespostaPonto;
import br.com.fabappu9.ecoloc.network.APIClient;
import dmax.dialog.SpotsDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static br.com.fabappu9.ecoloc.interfaces.Constantes.CHAVE_PONTO_RECUPERADO;

public class DetalhesEcoPontoActivity extends AppCompatActivity {

    private EditText edtNomePonto;
    private EditText editTxtEndereco;
    private TextView txtQtdLikes;
    private Button btnCadastrarPonto;
    private Button btnRemoverPonto;
    private ListView list_checkBox;
    private ImageButton btnLike;
    private ImageButton btnDislike;
    private SpotsDialog dialog;

    private Call<Resposta> retorno;
    private Call<Resposta> resposta;
    private Call<Resposta> retornoPontoRemovido;


    private PontoDto pontoRecuperado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_eco_ponto);

        iniciarComponentes();

        pontoRecuperado = (PontoDto) getIntent().getSerializableExtra(CHAVE_PONTO_RECUPERADO);

        edtNomePonto.setText(pontoRecuperado.getDescricao());
        String latitudeTexto = pontoRecuperado.getLatitude();
        String longitudeTexto = pontoRecuperado.getLongitude();

        double latitude = Double.parseDouble(latitudeTexto);
        double longitude = Double.parseDouble(longitudeTexto);

        String enderecoRecuperado = Localizador.encontrarEndereco(this, latitude, longitude);
        editTxtEndereco.setText(enderecoRecuperado);
        txtQtdLikes.setText("Likes: " + pontoRecuperado.getGostei());

        btnCadastrarPonto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pontoRecuperado.setDescricao(edtNomePonto.getText().toString());
                //pensar o que fazer se ele mudar o endereço

                alteraPonto();
            }
        });

        btnRemoverPonto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                excluirPonto();
            }
        });

        btnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realizaLike();

            }
        });

        btnDislike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                realizaDislike();
            }
        });

    }

    private void excluirPonto() {
        //http://janjrs.000webhostapp.com/ws_app/v1/ponto.php?CHAVE=12345&CHAMADA=DELETEPONTO&IDPONTO=91
        dialog.show();
        retornoPontoRemovido = new APIClient().getRestService().removePonto(
                "12345",
                "DELETEPONTO",
                pontoRecuperado.getId()
        );
        configurarCallbackRemovePonto(retornoPontoRemovido);
    }


    private void alteraPonto() {
        dialog.show();
        resposta = new APIClient().getRestService().alteraPonto(
                "12345",
                "UPDATEPONTO",
                pontoRecuperado.getDescricao(),
                pontoRecuperado.getLatitude(),
                pontoRecuperado.getLongitude(),
                "8",
                pontoRecuperado.getId()
        );
        configurarCallbackAlteraPonto(resposta);
    }


    private void iniciarComponentes() {
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        dialog = new SpotsDialog(this,  R.style.estilo_carregando);
        btnLike = (ImageButton) findViewById(R.id.btnLike);
        edtNomePonto = (EditText) findViewById(R.id.edtNomePonto);
        editTxtEndereco = (EditText) findViewById(R.id.editTxtEndereco);
        btnCadastrarPonto = (Button) findViewById(R.id.btnConfirmar);
        btnRemoverPonto = (Button) findViewById(R.id.btnRemoverPonto);
        list_checkBox = (ListView) findViewById(R.id.list_checkBox);
        txtQtdLikes = (TextView) findViewById(R.id.txtQtdLikes);
        btnDislike = (ImageButton) findViewById(R.id.btnDislike);

    }

    private void realizaLike() {
        dialog.show();
        retorno = new APIClient().getRestService().realizaLikeOuDislike(
                "12345",
                "SETLIKE",
                "SETLIKE",
                pontoRecuperado.getId()
        );
        configurarCallbackLike(retorno);
    }

    private void realizaDislike() {
        dialog.show();
        retorno = new APIClient().getRestService().realizaLikeOuDislike(
                "12345",
                "SETLIKE",
                "SETDISLIKE",
                pontoRecuperado.getId()
        );
        configurarCallbackDisLike(retorno);
    }

    private void configurarCallbackLike(Call<Resposta> retorno) {
        retorno.enqueue(new Callback<Resposta>() {
            @Override
            public void onResponse(Call<Resposta> call, Response<Resposta> response) {
                dialog.hide();
                String gosteiTexto = pontoRecuperado.getGostei();
                int qtdGOstei = Integer.parseInt(gosteiTexto);
                qtdGOstei = qtdGOstei + 1;
                txtQtdLikes.setText("Likes: " + qtdGOstei);

                btnLike.setImageResource(R.drawable.ic_like_valido);
                btnLike.setEnabled(false);

                pontoRecuperado.setGostei(String.valueOf(qtdGOstei));
            }

            @Override
            public void onFailure(Call<Resposta> call, Throwable t) {
                Toast.makeText(DetalhesEcoPontoActivity.this, "Algum erro aconteceu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarCallbackDisLike(Call<Resposta> retorno) {
        retorno.enqueue(new Callback<Resposta>() {
            @Override
            public void onResponse(Call<Resposta> call, Response<Resposta> response) {
                dialog.hide();
                String gosteiTexto = pontoRecuperado.getGostei();
                int qtdGOstei = Integer.parseInt(gosteiTexto);
                qtdGOstei = qtdGOstei - 1;
                txtQtdLikes.setText("Likes: " + qtdGOstei);

                pontoRecuperado.setGostei(String.valueOf(qtdGOstei));
                btnDislike.setImageResource(R.drawable.ic_dislike_valido);
                btnDislike.setRotation(180);
                btnDislike.setEnabled(false);

            }

            @Override
            public void onFailure(Call<Resposta> call, Throwable t) {
                Toast.makeText(DetalhesEcoPontoActivity.this, "Algum erro aconteceu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarCallbackAlteraPonto(Call<Resposta> resposta) {
        resposta.enqueue(new Callback<Resposta>() {
            @Override
            public void onResponse(Call<Resposta> call, Response<Resposta> response) {
                dialog.hide();
                setResult(RESULT_OK);
                mostrarMensagemAgradecimento();
            }

            @Override
            public void onFailure(Call<Resposta> call, Throwable t) {
                dialog.hide();
                Toast.makeText(DetalhesEcoPontoActivity.this, "Algum erro aconteceu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    private void configurarCallbackRemovePonto(Call<Resposta> resposta) {
        resposta.enqueue(new Callback<Resposta>() {
            @Override
            public void onResponse(Call<Resposta> call, Response<Resposta> response) {
                dialog.hide();
                Toast.makeText(DetalhesEcoPontoActivity.this, "Ponto removido", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.putExtra("pontoRemover", pontoRecuperado);
                setResult(RESULT_OK, intent);
                finish();
            }

            @Override
            public void onFailure(Call<Resposta> call, Throwable t) {
                dialog.hide();
                Toast.makeText(DetalhesEcoPontoActivity.this, "Algum erro aconteceu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }
        });
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
        AlertDialog.Builder builder = new AlertDialog.Builder(DetalhesEcoPontoActivity.this);
        builder.setView(view);
        builder.create().show();
    }

}
