package pridbank.controller;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import pridbank.model.entity.Compte;

@Named
@RequestScoped
public class CompteBacking extends BaseBacking {
    
    private static final long serialVersionUID = 1L;
    
    private Integer numero;
    private Character type;
    private Float solde;
    private Boolean estBloque;
    private Compte compteSelectionne;
    private List<String> listeTypes = new ArrayList<>();
    
    public CompteBacking() {
        listeTypes.add("Restreint");
        listeTypes.add("Normal");
        listeTypes.add("Gold");
    }
    
    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Character getType() {
        return type;
    }

    public void setType(Character type) {
        this.type = type;
    }

    public Float getSolde() {
        return solde;
    }

    public void setSolde(Float solde) {
        this.solde = solde;
    }

    public Boolean getEstBloque() {
        return estBloque;
    }

    public void setEstBloque(Boolean estBloque) {
        this.estBloque = estBloque;
    }
    
    public List<Compte> getListeComptes(){
        return model.getListeComptes();
    }
    
    public Compte getCompteSelectionne() {
        return compteSelectionne;
    }

    public void setCompteSelectionne(Compte compteSelectionne) {
        this.compteSelectionne = compteSelectionne;
    }
    
    public List<String> getListeTypes() {
        return listeTypes;
    }

    public void setListeTypes(List<String> listeTypes) {
        this.listeTypes = listeTypes;
    }
}
