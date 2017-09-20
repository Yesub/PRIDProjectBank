function inverserChamp(champ) {
    var element = document.getElementById(champ);
    if(element.style.display === 'none') {
        validerChamp(champ);
    } else {
        editerChamp(champ);
    }
}

function editerChamp(champ) {
    var element = document.getElementById(champ);
    var elementEditable = document.getElementById(champ + '_edit');
    elementEditable.value = element.value;
    element.style.display = 'none';
    elementEditable.style.display = 'inline-block';
}

function validerChamp(champ) {
    var element = document.getElementById(champ);
    var elementEditable = document.getElementById(champ + '_edit');
    element.value = elementEditable.value;
    elementEditable.style.display = 'none';
    element.style.display = 'inline-block';
    rafraichirBouton(champ);
}

function rafraichirBouton(champOriginal) {
    // devra vérifier via un appel Ajax si le contenu de la case correspondant au champ est différent du champ initial
    // et rendre le bouton de Submit clicable ou non en fonction
}