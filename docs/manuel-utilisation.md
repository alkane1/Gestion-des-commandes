# Manuel d'utilisation

## 1. Presentation de l'application

`Gestion_des_commandes` est une application Android de demonstration pour organiser des commandes dans des conteneurs d'expedition.

L'application permet de :

- consulter une liste de commandes generees automatiquement
- filtrer et rechercher les commandes
- regler les parametres des conteneurs
- calculer un plan de chargement
- comparer plusieurs solutions de chargement
- visualiser le detail des conteneurs et des commandes

L'objectif principal est de maximiser la valeur expediee tout en respectant les contraintes de poids, de volume et de priorite.

## 2. Ecrans principaux

### 2.1 Ecran Commandes

Cet ecran est le point d'entree principal.

Fonctions disponibles :

- afficher la liste des commandes
- rechercher une commande par identifiant ou par note
- filtrer par priorite : `Toutes`, `Urgent`, `Elevee`, `Normale`
- regenerer un nouveau jeu de commandes
- lancer le calcul du plan
- activer ou desactiver le theme sombre

Chaque carte de commande affiche :

- l'identifiant de la commande
- sa priorite
- son poids
- son volume
- sa valeur en euros
- l'indication `Fragile` si besoin

### 2.2 Ecran Conteneurs

Cet ecran permet de configurer les regles de chargement.

Parametres disponibles :

- capacite poids maximale par conteneur
- capacite volume maximale par conteneur
- seuil minimal de remplissage
- nombre total de conteneurs

Le bouton `Sauvegarder` enregistre ces preferences localement. Elles sont restaurees automatiquement lors du prochain lancement.

### 2.3 Ecran Plan

Cet ecran presente le resultat du calcul.

Elements affiches :

- recette totale estimee
- nombre de conteneurs
- nombre de commandes expediees
- nombre de commandes reportees
- remplissage moyen
- solutions proposees
- liste detaillee des conteneurs
- apercu des commandes reportees

Depuis cet ecran, l'utilisateur peut :

- recalculer un plan
- demander plusieurs solutions
- appliquer une solution proposee
- ouvrir le detail d'un conteneur avec le bouton `Voir`

### 2.4 Ecran Detail commande

Cet ecran affiche une commande unique avec :

- son identifiant
- sa priorite
- son poids
- son volume
- son prix
- son statut fragile
- sa note

### 2.5 Ecran Detail conteneur

Cet ecran reprend les informations d'un conteneur :

- commandes presentes
- recette
- taux de remplissage poids et volume
- bouton `Retour` pour revenir au plan

## 3. Parcours d'utilisation recommande

1. Aller dans l'ecran `Commandes`.
2. Verifier ou regenerer les commandes.
3. Ouvrir l'ecran `Conteneurs`.
4. Ajuster les capacites et le seuil de remplissage.
5. Sauvegarder la configuration.
6. Revenir au plan ou lancer le calcul depuis l'ecran `Commandes`.
7. Consulter les solutions proposees.
8. Appliquer la solution la plus interessante.
9. Ouvrir les conteneurs pour verifier le detail du chargement.

## 4. Description de l'algorithme de remplissage

## 4.1 Objectif

L'algorithme cherche a construire un plan de chargement qui :

- respecte les capacites de poids et de volume
- expedie les commandes prioritaires en premier
- maximise la recette expediee
- evite d'envoyer des conteneurs trop peu remplis si aucune priorite ne l'impose

## 4.2 Donnees d'entree

L'algorithme utilise :

- la liste des commandes
- la configuration des conteneurs
- une heuristique de score

Chaque commande contient :

- un poids
- un volume
- un prix
- une priorite

## 4.3 Etape 1 : creation des conteneurs

Le systeme cree autant de conteneurs que defini par `containerCount`.

Chaque conteneur possede :

- une capacite maximale de poids
- une capacite maximale de volume
- une liste de commandes chargees

## 4.4 Etape 2 : tri des commandes prioritaires

Les commandes sont separees en deux groupes :

- prioritaires : `URGENT` et `ELEVEE`
- normales

Les commandes prioritaires sont traitees avant les commandes normales.

## 4.5 Etape 3 : placement glouton

L'algorithme utilise une premiere phase dite "greedy".

Pour chaque commande :

- un score est calcule a partir du prix, du poids, du volume et de la priorite
- les commandes avec le meilleur score sont traitees d'abord
- la commande est placee dans le conteneur qui l'accueille au mieux sans depasser les limites

Une commande qui ne rentre dans aucun conteneur est marquee comme restante.

## 4.6 Etape 4 : optimisation locale

Une seconde phase d'amelioration est appliquee.

Elle teste des modifications locales entre conteneurs :

- `move` : deplacer une commande normale d'un conteneur vers un autre
- `swap` : echanger deux commandes normales entre deux conteneurs

Ces operations sont conservees seulement si elles ameliorent le score global.

Le score favorise :

- la recette expediee
- le nombre de commandes expediees
- un meilleur remplissage moyen
- moins de conteneurs faiblement remplis

## 4.7 Etape 5 : application de la regle de seuil

Une fois le chargement termine :

- si un conteneur contient une commande prioritaire, il peut partir meme si son remplissage est faible
- si un conteneur ne contient aucune priorite et reste sous le seuil minimal, ses commandes sont reportees

## 4.8 Resultat final

Le resultat est un `ShipmentPlan` contenant :

- la liste des conteneurs expedies
- la liste des commandes expediees
- la liste des commandes reportees
- la recette totale

## 5. Solutions proposees

L'application peut proposer plusieurs variantes de calcul :

- `Equilibre`
- `Poids prioritaire`
- `Volume prioritaire`
- `Prix agressif`

Chaque solution utilise des coefficients heuristiques differents.

L'utilisateur peut comparer :

- la recette
- le remplissage moyen
- le nombre de reports

puis appliquer la solution la plus adaptee.

## 6. Sauvegarde locale

L'application sauvegarde localement :

- le theme clair ou sombre
- la configuration des conteneurs

Cette sauvegarde est geree avec `DataStore`.

## 7. Limites actuelles

Version actuelle :

- les commandes sont generees automatiquement
- il n'y a pas encore de base de donnees metier persistante pour les commandes
- l'algorithme reste heuristique et non optimal mathematiquement

## 8. Conclusion

L'application fournit une base claire pour simuler, comparer et visualiser des plans de chargement de commandes dans des conteneurs.

Elle combine :

- une interface mobile moderne
- une configuration simple
- une logique de priorisation metier
- une heuristique de remplissage enrichie par une optimisation locale
