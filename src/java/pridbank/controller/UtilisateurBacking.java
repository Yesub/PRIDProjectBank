package pridbank.controller;

import java.util.Date;
import java.util.List;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import pridbank.model.entity.Compte;
import pridbank.model.entity.Utilisateur;

@Named
@RequestScoped
public class UtilisateurBacking extends BaseBacking {

    private static final long serialVersionUID = 1L;

    private String nomUtilisateur;
    private String motDePasse;
    private Date dateDeNaissance;
    private Boolean estUnGestionnaire;
    private Utilisateur utilisateurSelectionne;

    public String getNomUtilisateur() {
        return nomUtilisateur;
    }

    public void setNomUtilisateur(String nomUtilisateur) {
        this.nomUtilisateur = nomUtilisateur;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public Date getDateDeNaissance() {
        return dateDeNaissance;
    }

    public void setDateDeNaissance(Date dateDeNaissance) {
        this.dateDeNaissance = dateDeNaissance;
    }

    public Boolean getEstUnGestionnaire() {
        return estUnGestionnaire;
    }

    public void setEstUnGestionnaire(Boolean estUnGestionnaire) {
        this.estUnGestionnaire = estUnGestionnaire;
    }
    
//    public List<Utilisateur> getListeUtilisateurs(){
//        return model.getListeUtilisateurs();
//    }
    
    public List<Compte> getListeComptes(){
        return model.getComptesDuClient(utilisateurCourant);
    }
    
    public Utilisateur getUtilisateurSelectionne() {
        return utilisateurSelectionne;
    }
 
    public void setUtilisateurSelectionne(Utilisateur utilisateurSelectionne) {
        this.utilisateurSelectionne = utilisateurSelectionne;
    }
}
