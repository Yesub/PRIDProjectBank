package pridbank.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.UnselectEvent;
import pridbank.model.entity.Compte;
import pridbank.model.entity.Utilisateur;
import pridbank.model.entity.Virement;

@Named(value = "clientBacking")
@ViewScoped
public class ClientBacking extends BaseBacking {
    
    @Inject
    DateFactice dateFactice;
    
    private Compte compteSelectionne;
    private List<Compte> compteNonBloque;
    private Utilisateur clientSelectionne;
    private List<Compte> comptesClientSelectionne = new ArrayList<>();
    private Compte compteDestinataire;
    private int numeroCompteDestinataire;
    private float sommeVirement;
    private Date dateExecution;
    private List<Virement> listeVirements = new ArrayList<>();
    private Float sommeDisponiblePourVirement;
    
    public Float getSommeDisponiblePourVirement() {
        return sommeDisponiblePourVirement;
    }

    public void setSommeDisponiblePourVirement(Float sommeDisponiblePourVirement) {
        this.sommeDisponiblePourVirement = sommeDisponiblePourVirement;
    }
    
    @PostConstruct
    public void postConstruct() {
        this.dateExecution = this.dateFactice.getDateFactice();
    }
    
    public List<Virement> getListeVirements() {
        return listeVirements;
    }

    public void setListeVirements(List<Virement> listeVirements) {
        this.listeVirements = listeVirements;
    }
    
    public Date getDateExecution() {
        return dateExecution;
    }

    public void setDateExecution(Date dateExecution) {
        this.dateExecution = dateExecution;
    }

    public float getSommeVirement() {
        return sommeVirement;
    }

    public void setSommeVirement(float sommeVirement) {
        this.sommeVirement = sommeVirement;
    }

    public Compte getCompteDestinataire() {
        return compteDestinataire;
    }

    public void setCompteDestinataire(Compte compteDestinataire) {
        this.compteDestinataire = compteDestinataire;
        if(compteDestinataire != null) {
            this.numeroCompteDestinataire = compteDestinataire.getNumero();
        }
    }
    
    public int getNumeroCompteDestinataire() {
        return numeroCompteDestinataire;
    }

    public void setNumeroCompteDestinataire(int numeroCompteDestinataire) {
        this.numeroCompteDestinataire = numeroCompteDestinataire;
    }

    public List<Compte> getComptesClientSelectionne() {
        return comptesClientSelectionne;
    }

    public Utilisateur getClientSelectionne() {
        return clientSelectionne;
    }

    public void setClientSelectionne(Utilisateur clientSelectionne) {
        this.clientSelectionne = clientSelectionne;
    }
   

    public List<Compte> getCompteNonBloque() {
        compteNonBloque = new ArrayList<>();
        getListeComptes().stream().filter((compte) -> (!compte.getEstBloque())).forEach((compte) -> {
            compteNonBloque.add(compte);
        });
        return compteNonBloque;
    }

    public Compte getCompteSelectionne() {
        return compteSelectionne;
    }

    public void setCompteSelectionne(Compte compteSelectionne) {
        this.compteSelectionne = compteSelectionne;
    }
    
    public boolean isCompteSelectionneUnset(){
        return compteSelectionne != null;
    }
    
    public List<Compte> getListeComptes(){
        return model.getComptesDuClient(utilisateurCourant);
    }
    
    public List<Utilisateur> getListeClients(){
        return model.getClients();
    }
    
    public void onRowSelectCompte(SelectEvent event) {
        this.compteSelectionne = (Compte) event.getObject();
        this.sommeDisponiblePourVirement = calculerSommeDisponiblePourVirement();
        this.genererListeVirements();
    }
    
    public void onRowUnselectCompte(UnselectEvent event) {
        this.compteSelectionne = null;
    }
    
    public void onRowSelectUtilisateur(SelectEvent event) {
        this.clientSelectionne = (Utilisateur) event.getObject();
        this.comptesClientSelectionne.clear();
        for(Compte compte : this.clientSelectionne.getListeDesComptes()) {
            if(!compte.getEstBloque()) {
                comptesClientSelectionne.add(compte);
            }
        }
    }
    
    public void onRowUnselectUtilisateur(UnselectEvent event) {
        this.clientSelectionne = null;
    }
    
    public void onRowSelectCompteDestinataire(SelectEvent event) {
        this.compteDestinataire = (Compte) event.getObject();
        this.numeroCompteDestinataire = this.compteDestinataire.getNumero();
    }
    
    public void onRowUnselectCompteDestinataire(UnselectEvent event) {
        this.compteDestinataire = null;
    }
    
    public void onChangeNumeroCompteDestinataire() {
        Compte compte = model.trouverCompte(numeroCompteDestinataire);
        if(compte == null) {
            this.compteDestinataire = null;
               model.afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "Ce numéro de compte n'existe pas."));
        } else {
            this.compteDestinataire = compte;
        }
    }
    
    public void changerDate(SelectEvent event) {
        this.dateExecution = (Date) event.getObject();
        if(this.dateExecution.after(this.dateFactice.getDateFactice())) {
            this.sommeDisponiblePourVirement = 10000.0f;
        }
        if(this.dateExecution.before(this.dateFactice.getDateFactice())) {
            model.afficherMessage(new FacesMessage(FacesMessage.SEVERITY_INFO, "Info", "La date ne peut pas être inférieure à la date actuelle."));
        }
    }
    
    public void closeModaleVirement(){
        genererListeVirements();
    }
    
    public float calculerSommeDisponiblePourVirement() {
        return model.sommeDisponiblePourVirement(compteSelectionne);
    }
    
    public void nouveauFormulaire() {
        compteSelectionne = null;
        compteNonBloque = null;
        clientSelectionne = null;
        comptesClientSelectionne = new ArrayList<>();
        compteDestinataire = null;
        numeroCompteDestinataire = 0;
        sommeVirement = 0;
        dateExecution = this.dateFactice.getDateFactice();
        listeVirements.clear();
    }
    
    public void effectuerVirement() {
        if(model.preparerExecutionVirement(compteSelectionne, compteDestinataire, sommeVirement, dateExecution)) {
            nouveauFormulaire();
            this.utilisateurCourant = this.refreshUtilisateur(utilisateurCourant);
        }
    }
    
    public void genererListeVirements() {
        this.listeVirements.clear();
        if (compteSelectionne != null) {
            if(!compteSelectionne.getVirementsEntrants().isEmpty()){
                for(Virement virementEntrant : compteSelectionne.getVirementsEntrants()) {
                    if (verifierVirementEntrantPourListeVirement(virementEntrant)) { 
                        this.listeVirements.add(virementEntrant);
                    }
                }
            }
            if(!compteSelectionne.getVirementsSortants().isEmpty()){
                for(Virement virementSortant : compteSelectionne.getVirementsSortants()) {
                    this.listeVirements.add(virementSortant);
                }
            }
        }        
    }
    
    public boolean verifierVirementEntrantPourListeVirement(Virement virement) {
        return virement.getDateexecution() != null;
    }
    
    public void supprimerVirement(Virement virement){
        model.supprimerVirement(virement);
        genererListeVirements();
    }

    public String afficherMontantVirement(){
        return String.format(Locale.FRANCE,"%.2f", this.sommeVirement) + " €";
    }
    
    public void refreshAll() {
        model.clearCache();
    }
    
    public boolean desactiverBoutonValiderVirement() {
        return model.formulaireVirementInvalide(compteSelectionne, compteDestinataire, sommeVirement, dateExecution);
    }
}
