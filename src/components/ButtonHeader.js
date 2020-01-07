import React from "react";
import { Text, TouchableOpacity } from "react-native";
import { withNavigation } from "react-navigation";
import PropTypes from "prop-types";
import moment from "moment";
import _ from "lodash";

class ButtonHeader extends React.Component {

    static propTypes = {
        item : PropTypes.object.isRequired
    };

    constructor(props) {
        super(props);
        this.state = {
            produits: [
                {id:1, num:1, name:"GO", label:"GO", selected: false, used: false},
                {id:2, num:2, name:"ADB", label:"ADBlue", selected: false, used: false},
                {id:3, num:3, name:"SSP", label:"SSP", selected: false, used: false},
                {id:4, num:4, name:"FOD", label:"GNR", selected: false, used: false},
                {id:17, num:4, name:"GNR", label:"GNR", selected: false, used: false},
                {id:5, num:10, name:"LUB", label:"Lubrifiant", selected: false, used: false},
                {id:16, num:20, name:"GAZ", label:"GAZ", selected: false, used: false},
                {id:6, num:30, name:"LAV", label:"Lavage", selected: false, used: false},
                {id:7, num:30, name:"LA", label:"Lavage", selected: false, used: false},
                {id:8, num:31, name:"ENT", label:"Entretien", selected: false, used: false},
                {id:9, num:32, name:"VID", label:"Vidange", selected: false, used: false},
                {id:10, num:32, name:"VI", label:"Vidange", selected: false, used: false},
                {id:11, num:41, name:"BOUTIQUE", label:"Boutique", selected: false, used: false},
                {id:12, num:42, name:"BOUTIQUE1", label:"Boutique1", selected: false, used: false},
                {id:13, num:43, name:"BOUTIQUE2", label:"Boutique2", selected: false, used: false},
                {id:14, num:44, name:"BOUTIQUE3", label:"Boutique3", selected: false, used: false},
                {id:15, num:45, name:"CARBURANT", label:"Carburant", selected: false, used: false},
            ],
            produitsActifs : []
        };

       // this._manageProducts(this.props.item);
    }

  /*  _manageProducts(i) {
        let arrayProducts = i.restrictionCode;
        let produits = [...this.state.produits];
        let produitsActifs = [];
        if(i.restrictionCode.includes("TSERV")){
            this._manageAdvanced(i)
        }
        else if(i.restrictionCode.includes("_") && !i.restrictionCode.includes("TSERV")){
            arrayProducts = i.restrictionCode.split('_');

            arrayProducts.forEach(function(element) { //parcours les produits
                //pour chaque produit on parcours la liste -> true
                _.each(produits, function(produit, index){
                    if(produit.name === element){
                        produitsActifs.push(produit);
                    }
                    else if(element === "CARBURANT" || element === "CARB"){
                        switch (produit.name) {
                            case "GO":
                            case "SSP":
                            case "FOD":
                                produitsActifs.push(produit);
                        }
                    }
                });
            });
            this.state = {produitsActifs : produitsActifs};
        }
        else {
            _.each(produits, function(produit, index){
                if(produit.name === arrayProducts){
                    produitsActifs.push(produit);
                }
                else if(arrayProducts === "CARBURANT" || arrayProducts === "CARB") {
                    switch (produit.name) {
                        case "GO":
                        case "SSP":
                        case "FOD":
                            produitsActifs.push(produit);
                    }
                }
            });
            this.state = {produitsActifs : produitsActifs};
        }
    }

    _manageAdvanced(i){
        let produitsActifs = [];
        let arrayProducts = i.restrictionCode;
        let produits = [...this.state.produits];

        if(i.restrictionCode.includes("_")){ //if tableau
            // on parcours
            arrayProducts = i.restrictionCode.split('_');

            arrayProducts.forEach(function(element) {
                if(element === "GO"){
                    _.each(produits, function(produit, index){
                        if(produit.name === "GO") {
                            produitsActifs.push(produit);
                        }
                    });
                }
                else if(element === "SSP"){
                    _.each(produits, function(produit, index){
                        if(produit.name === "SSP") {
                            produitsActifs.push(produit);
                        }
                    });
                }
                else if(element === "TSERV"){
                    _.each(produits, function(produit, index){
                        switch (produit.name) {
                            case "ADB":
                            case "LUB":
                            case "VID":
                            case "LAV":
                            case "GAZ":
                            case "ENT":
                            case "BOUTIQUE":
                            case "BOUTIQUE1":
                            case "BOUTIQUE2":
                            case "BOUTIQUE3":
                                produitsActifs.push(produit);
                        }
                    });
                }
            });
        }
        else { // on active tout
            _.each(produits, function(produit, index){
                switch (produit.name) {
                    case "GO":
                    case "ADB":
                    case "SSP":
                    case "FOD":
                    case "LUB":
                    case "VID":
                    case "LAV":
                    case "GAZ":
                    case "ENT":
                    case "BOUTIQUE":
                    case "BOUTIQUE1":
                    case "BOUTIQUE2":
                    case "BOUTIQUE3":
                        produitsActifs.push(produit);
                }
            });
        }
        this.state = {produitsActifs : produitsActifs};
    }*/

    _goNext() {
        //console.log(this.props.produits);
        this.props.navigation.navigate('NewOrder', {item: this.props.item});
    };

    render() {
        const { item } = this.props;
        let opposition = false;

      /*  if(item.dateOpposition.trim().length !== 0){
            opposition = true;
        }*/

        if (item.status === "ACTIVE") {
            return (
                <TouchableOpacity style={{
                    width: 160, height: 45, backgroundColor: "#43a047",
                    justifyContent: 'center', alignItems: 'center', marginRight: 10, borderRadius: 5}} onPress={() => this._goNext()}>
                    <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 14}}>Suivant</Text>
                </TouchableOpacity>
            );
        }
        else {
            return (
                <TouchableOpacity style={{
                    width: 160, height: 45, backgroundColor: "#e53935",
                    justifyContent: 'center', alignItems: 'center', marginRight: 10, borderRadius: 5}} onPress={ () => { this.props.navigation.goBack() }}>
                    <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 14}}>Retour</Text>
                </TouchableOpacity>
            );
        }
    }
    }

export default withNavigation(ButtonHeader);