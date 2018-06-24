package br.com.fabappu9.ecoloc.DTO;

import java.io.Serializable;

/**
 * Created by Janailson on 03/03/2018.
 */

public class PontoDto implements Serializable {

    private String id;
    private String descricao;
    private String tipomaterial;
    private String latitude;
    private String longitude;
    private String gostei;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoMaterial() {
        return tipomaterial;
    }

    public void setTipoMaterial(String tipomaterial) {
        this.tipomaterial = tipomaterial;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getGostei() {
        return gostei;
    }

    public void setGostei(String gostei) {
        this.gostei = gostei;
    }
}
