package pridbank.controller;

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.inject.Inject;

public class AuthorizationListener implements PhaseListener {

    private static final long serialVersionUID = 1L;

    @Inject
    private LogInBacking backing;

    @Override
    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        String currentPage = context.getViewRoot().getViewId();
        NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
        if (backing.getUtilisateurCourant() == null && currentPage.startsWith("/protected")) {
            navigationHandler.handleNavigation(context, null, "/index.xhtml?faces-redirect=true");
        } else if (backing.getUtilisateurCourant() != null && backing.getUtilisateurCourant().getEstUnGestionnaire() && currentPage.startsWith("/protected/client/")) {
            navigationHandler.handleNavigation(context, null, "/protected/index.xhtml?faces-redirect=true");
        } else if (backing.getUtilisateurCourant() != null && !backing.getUtilisateurCourant().getEstUnGestionnaire() && currentPage.startsWith("/protected/gestionnaire/")) {
            navigationHandler.handleNavigation(context, null, "/protected/index.xhtml?faces-redirect=true");
        }
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        //Nothing ...
    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
}
