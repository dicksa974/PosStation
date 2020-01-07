import React from "react";
import {ActivityIndicator, ScrollView, StyleSheet, Text, TouchableOpacity, View, Image} from "react-native";
import { Card, Icon, Tooltip } from 'react-native-elements/src/index';
import { connect } from "react-redux";
import moment from "moment/moment";
import Modal from "react-native-modalbox";
import FontAwesome5 from 'react-native-vector-icons/FontAwesome5';
import _ from "lodash";
import { host } from "../utils/constants";
import FontAwesome from "react-native-vector-icons/FontAwesome";
import Ionicons from "react-native-vector-icons/Ionicons";

class listTransactions extends React.Component {

    static navigationOptions = ({ navigation, screensProps }) => ({
        headerLeft: <Icon type={'simple-line-icon'} name={'arrow-left'} color={'black'} onPress={ () => { navigation.goBack() }}/>,
        headerRight:<Icon type={'simple-line-icon'} name={'home'} color={'#03498e'} onPress={ () => { navigation.navigate("Home") }} containerStyle={{marginRight:20}} />,
        headerTitle:
          <View style={{flex:1, flexDirection:'row'}}>
              <Image source={require('../../assets/images/iconx96.jpg')} style={{width:55, height:55, borderRadius:8, marginTop:10}} resizeMode={"contain"}/>
              <Text style={{color:'#03498e', fontSize:20, marginLeft:15, fontFamily:'Livvic-Medium', textAlignVertical:'center'}}>LES DERNIERES TRANSACTIONS</Text>
          </View>,
        headerTransparent: true
    });

    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            transactions: [],
            debits: {count:0, somme:0},
            actives: {count:0, somme:0},
            recharges: {count:0, somme:0},
            annuls: {count:0, somme:0},
            ticketTransacs: [],
            carte:"",
            type:"",
            produits: [
                {id:1, num:1, name:"GO", label:"GO", selected: false, color:'#fbc02d'},
                {id:2, num:2, name:"ADB", label:"ADBlue", selected: false, color:'#1976d2'},
                {id:3, num:3, name:"SSP", label:"SSP", selected: false, color:'#4caf50'},
                {id:4, num:4, name:"FOD", label:"GNR", selected: false, color:'#616161'},
                {id:5, num:10, name:"LUB", label:"Lubrifiant", selected: false, color:'#fff'},
                {id:16, num:20, name:"GAZ", label:"GAZ", selected: false, color:'#fff'},
                {id:6, num:30, name:"LAV", label:"Lavage", selected: false, color:'#fff'},
                {id:8, num:31, name:"ENT", label:"Entretien", selected: false, color:'#fff'},
                {id:9, num:32, name:"VID", label:"Vidange", selected: false, color:'#fff'},
                {id:11, num:41, name:"BOUTIQUE", label:"Boutique", selected: false, color:'#fff'},
                {id:12, num:42, name:"BOUTIQUE1", label:"Boutique1", selected: false, color:'#fff'},
                {id:13, num:43, name:"BOUTIQUE2", label:"Boutique2", selected: false, color:'#fff'},
                {id:14, num:44, name:"BOUTIQUE3", label:"Boutique3", selected: false, color:'#fff'},
            ],
            showErr:false,
            station: {},
            currentTab: 1,
        };
    }

    async componentDidMount() {
        let now = moment();
        //console.log("ticket expired", this.props.expiredAt);
        //console.log("info ticket",this.props.ticket);
        //console.log("info token",this.props.token);
        if(now.isSameOrAfter(this.props.expiredAt)) {
            const action = { type: "REMOVE_TRANSACTION"};
            this.props.dispatch(action);
        }

        if(this.props.ticket){
            fetch(host + "/transactions/ticket/"+this.props.ticket, {
                method: "GET",
                headers: {
                    Accept : "application/json",
                    "Content-Type" : "application/json",
                    Authorization: "Bearer " + this.props.token
                }
            }).then((response) => {
                if(response.status === 200) {
                    response.json().then(data => {
                        this.setState({ ticketTransacs: data, carte: data[0].carte.serialNumber, type: data[0].typeTransaction})
                    });
                }
            }).catch(error => console.error(error));
        }

        //console.log("expired AT",this.props.expiredAt);
        //console.log("Mon dernier ticket",this.props.ticket);

        try {
            fetch(host + "/transactions/station/5ddfa08ecf4de44d374f313f", {
                method: "GET",
                headers: {
                    Accept : "application/json",
                    "Content-Type" : "application/json",
                    Authorization: "Bearer " + this.props.token
                }
            }).then((response) => {
                if(response.status === 200) {
                    response.json().then(data => {
                        this.setState({ transactions: data });
                        this._sortTransactions(data);
                    });
                }
                else if (response.status === 500){
                    this.setState({ showError500: true })
                }
                else {
                    this.setState({ showError400: true })
                }
            }).catch(error => console.error(error));
        } catch (err) {
            console.log('error: ', err)
        }
    }

    _sortTransactions(data){
        let nbDebit = 0, sumDebit = 0, nbActive = 0, sumActive = 0, nbRecharge = 0,  sumRecharge = 0, nbAnnul = 0,  sumAnnul = 0;

        data.forEach(element => {
            switch (element.typeTransaction) {
                 case 'DEBIT':
                     console.log(element);
                     nbDebit += 1;
                     sumDebit = sumDebit + element.montant;
                     let deb = {count:nbDebit, somme:sumDebit};
                     this.setState({ debits : deb });
                    break;
                case 'ACTIVATION':
                    nbActive += 1;
                    sumActive = sumActive + element.montant;
                    let act = {count:nbActive, somme:sumActive};
                    this.setState({ actives : act });
                    break;
                case 'RECHARGE':
                    nbRecharge += 1;
                    sumRecharge = sumRecharge + element.montant;
                    let rech = {count:nbRecharge, somme:sumActive};
                    this.setState({ recharges : rech });
                    break;
                case 'ANNULATION':
                    nbAnnul += 1;
                    sumAnnul = sumAnnul + element.montant;
                    let annul = {count:nbAnnul, somme:sumAnnul};
                    this.setState({ annuls : annul });
                    break;
            }
        });
    }

    _goToHome () {
        this.refs.modalSuccess.close();
        this.refs.modalFail.close();
        this.props.navigation.navigate('Home');
    }

     _cancelTransac (){
        try {
            console.log(this.props.ticket);
            fetch(host + "/tickets/annul/"+this.props.ticket, {
                method: "POST",
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + this.props.token
                }
            }).then((response) => {
                console.log(response);
                if (response.status === 200) {
                    const action = {type: "REMOVE_TRANSACTION"};
                    this.props.dispatch(action);
                    this.refs.modalAsk.close();
                    this.refs.modalSuccess.open();
                }
                else{
                    this.refs.modalAsk.close();
                    this.refs.modalFail.open();
                }
            })
                .catch(error => this.refs.modalFail.open());
        } catch (e) {
            console.error(e);
        }
    }

    _showModal(){
        this.refs.modalAsk.open();
}

    _showInfo(){
        this.setState({showErr:true});
    }

    onTabClick = (currentTab) => {
        this.setState({
            currentTab: currentTab,
        });
    };

    _showJournal(){
        this.refs.modalJournal.open();
    };

    render() {
        const { loading, transactions, showErr, annuls, debits, actives, recharges } = this.state;
        let disable  = true;
        let text = "";
        let now = moment();
         if(this.props.ticket !== null && now.isSameOrBefore(this.props.expiredAt)) {
             disable  = false;
             let montant = 0;
            //show tableau
             console.log(this.state.ticketTransacs);
             _.each(this.state.ticketTransacs, function(t, index) {
                 montant = montant + t.montant;
             });
             /*+ this.state.ticketTransacs[0].carte.serialNumber +*/
             text= "Confirmer l'annulation de "+montant.toFixed(2)+ " € pour la carte n° "+ this.state.carte+" ?"
         }
        return (
            <View style={{flex: 1, backgroundColor: '#fafafa'}}>
                <Modal style={{ height: 300, width: 550, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalAsk"} swipeToClose={false} backdropPressToClose={false}>
                    <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <FontAwesome5 name={"question-circle"} color={"#fb8c00"} size={35} style={{marginTop:15}}/>
                            <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Confirmer</Text>
                        </View>
                        <View style={{flex:2, justifyContent:'center', alignItems:'center', marginTop:20}}>
                            <View style={{flex:1}}>
                                <Text style={{ fontFamily: 'Livvic-Regular', color: '#757575',
                                    fontSize: 16}}>{text}</Text>

                            </View>
                        </View>
                        <View style={{flex:1, flexDirection:'row', justifyContent:'space-around', alignItems:'flex-end', marginBottom:10, marginTop:10}}>
                           <TouchableOpacity style={{width: 140, height: 50, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => { this._cancelTransac() }}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Confirmer</Text>
                            </TouchableOpacity>
                            <TouchableOpacity style={{width: 140, height: 50, marginLeft:20,  backgroundColor:'red', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => { this.refs.modalAsk.close(); }}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Annuler</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </Modal>
                <Modal style={{  height: 250, width: 350, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalSuccess"} swipeToClose={false} backdropPressToClose={false}>
                    <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <FontAwesome5 name={"check-circle"} color={"green"} size={35} style={{marginTop:15}}/>
                            <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Paiement Annulé !</Text>
                        </View>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <TouchableOpacity style={{width: 140, height: 50, marginTop:20, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => {this._goToHome()}}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Valider</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </Modal>
                <Modal style={{  height: 250, width: 350, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalFail"} swipeToClose={false} backdropPressToClose={false}>
                    <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <FontAwesome5 name={"exclamation-circle"} color={"green"} size={35} style={{marginTop:15}}/>
                            <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Paiement Non Annulé !</Text>
                        </View>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <TouchableOpacity style={{width: 140, height: 50, marginTop:20, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => {this._goToHome()}}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Valider</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </Modal>
                <Modal style={{ height: 300, width: 500, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalJournal"}  swipeToClose={false} backdropPressToClose={false}>
                    {!loading &&
                    <View style={{flex: 1, marginTop:15,}}>
                        <Text style={{textAlign:'center', fontSize:20}}>Journal de Vente</Text>
                        <View style={{flex:1, marginTop:15, marginLeft:15}}>
                                <Text style={{fontFamily:'Livvic-Regular', fontSize:18}}>Débit : {debits.count} - {debits.somme.toFixed(2).replace('.', ',')} €</Text>
                                <Text style={{fontFamily:'Livvic-Regular', fontSize:18}}>Annulation : {annuls.count} - {annuls.somme.toFixed(2).replace('.', ',')} €</Text>
                                <Text style={{fontFamily:'Livvic-Regular', fontSize:18}}>Activation : {actives.count} - {actives.somme.toFixed(2).replace('.', ',')} €</Text>
                                <Text style={{fontFamily:'Livvic-Regular', fontSize:18}}>Recharge : {recharges.count} - {recharges.somme.toFixed(2).replace('.', ',')} €</Text>
                        </View>
                        <View style={{flex:1, justifyContent:'center',  alignItems:'center'}}>
                            <TouchableOpacity style={{width: 200, height: 45, backgroundColor:'#004e66', justifyContent:'center', alignItems:'center', borderRadius:5}} onPress={ () => {this.refs.modalJournal.close();}}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Fermer</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                    }
                </Modal>
                {loading &&
                <View style={{flex: 1, backgroundColor: '#fafafa', justifyContent:'center', alignItems:'center'}}>
                    <ActivityIndicator color={'blue'} size={"large"}/>
                </View>
                }
                <View style={{width:"94%", height:1, backgroundColor: "#bdbdbd", marginLeft:'3%', marginTop:'6%'}}/>
                { !loading &&
                    <View style={{flex:4, padding:4, marginTop:10}}>
                        <View style={{flex:4, width:'95%', alignItems:'center', marginBottom: 10}}>
                            <View style={styles.headerTab}>
                                <View style={{flex:2, justifyContent: 'center', alignItems: 'center'}}><Text
                                    style={[styles.headerText,{marginLeft:50}]}>Date</Text></View>
                                <View style={{flex:1, justifyContent: 'center', alignItems: 'center'}}><Text
                                    style={styles.headerText}>Type</Text></View>
                                <View style={{flex:1, justifyContent: 'center', alignItems: 'center'}}><Text
                                    style={styles.headerText}>Carte</Text></View>
                                <View style={{flex:1, justifyContent: 'center', alignItems: 'center'}}><Text
                                    style={styles.headerText}>Produit</Text></View>
                                <View style={{flex:1, justifyContent: 'center', alignItems: 'center'}}><Text
                                    style={styles.headerText}>Montant</Text></View>
                                <View style={{flex:1, justifyContent: 'center', alignItems: 'center'}}/>
                            </View>
                            <ScrollView refreshing={true} style={{padding: 8}}>
                                {
                                    transactions.map((t, i) => {
                                        //console.log(t);
                                        let icon, color, label;
                                        /*if (t.type===102 || t.type === 105) {
                                            icon = 'arrow-right-circle';
                                            color = 'green';
                                            label = "Vente";
                                        }
                                        else {
                                            icon = 'arrow-left-circle';
                                            color = 'red';
                                            label = "Annulation";
                                        }*/

                                      //  let produit = this.state.produits.find(x => x.num === t.familleProduit);

                                        return (
                                            <Card containerStyle={{padding: 0, margin: 5, borderRadius:5}} key={i}>
                                                <View
                                                    style={{flex: 1, flexDirection: 'row', justifyContent: 'space-between', height: 60, padding: 10}}>
                                                    <View
                                                        style={{width: '20%', flexDirection: 'row', justifyContent: 'center', alignItems: 'center'}}>
                                                        <Icon type='simple-line-icon' name="calendar" color="#eeeeee"/>
                                                        <Text style={[styles.textContent, {fontSize: 14, marginLeft: 15}]}>{moment(t.createdDate).format("DD/MM/YYYY hh:mm")}</Text>
                                                    </View>
                                                    <View
                                                        style={{width: '15%', flexDirection: 'row', justifyContent: 'center', alignItems: 'center'}}>
                                                        {t.typeTransaction !== null &&
                                                        <Text style={[styles.textContent, {fontSize: 14}]}>{t.typeTransaction}</Text>
                                                        }
                                                    </View>
                                                    <View
                                                        style={{width: '10%', flexDirection: 'row', justifyContent: 'center', alignItems: 'center'}}>
                                                        {t.carte  !== null &&
                                                        <Text style={[styles.textContent, {fontSize: 14}]}>{t.carte.serialNumber}</Text>
                                                        }
                                                    </View>
                                                    <View
                                                        style={{width: '15%', flexDirection: 'row', justifyContent: 'center', alignItems: 'center'}}>
                                                        {t.produit !== null &&
                                                        <Text style={[styles.textContent, {
                                                            fontSize: 14,
                                                            fontWeight: 'bold'
                                                        }]}>{t.produit.code}</Text>
                                                        }
                                                    </View>
                                                    <View
                                                        style={{width: '10%', flexDirection: 'row', justifyContent: 'center', alignItems: 'center'}}>
                                                        {t.typeTransaction !== 'ACTIVATION' &&
                                                        <Text
                                                            style={[styles.textContent, {fontSize: 14}]}>{t.montant.toFixed(2).replace('.', ',')} €</Text>
                                                        }
                                                    </View>
                                                    <View
                                                        style={{width: '5%', flexDirection: 'row', alignItems: 'center', justifyContent: 'flex-end'}}>
                                                       {/* <Icon type='simple-line-icon' name={icon} color={color}/>*/}
                                                    </View>
                                                </View>
                                            </Card>
                                        )
                                    })
                                }
                            </ScrollView>
                        </View>
                        <View style={{flex:1, width:'100%', justifyContent:'center',  alignItems:'center'}}>
                            <View style={{flexDirection:'row', justifyContent:'center',  alignItems:'center'}}>
                                <View style={{flex:1, justifyContent:'center',  alignItems:'center'}}>
                                    <TouchableOpacity style={{width: 250, height: 45, backgroundColor:'#004e66', justifyContent:'center', alignItems:'center', borderRadius:5}} onPress={ () => { this._showJournal()}}>
                                        <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Journal de Vente</Text>
                                    </TouchableOpacity>
                                </View>
                                <View style={{flex:1, justifyContent:'center',  alignItems:'center'}}>
                                    { disable === true &&
                                    <TouchableOpacity style={{width: 255, height: 45, backgroundColor:'#616161', justifyContent:'center', alignItems:'center', borderRadius:5}} onPress={ () => {this._showInfo()}}>
                                        <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Annuler la dernière transaction</Text>
                                    </TouchableOpacity>
                                    }
                                    { disable === false &&
                                    <TouchableOpacity style={{
                                        width: 250,
                                        height: 45,
                                        backgroundColor: '#f4511e',
                                        justifyContent: 'center',
                                        alignItems: 'center',
                                        borderRadius: 5
                                    }} onPress={() => {
                                        this._showModal()
                                    }}>
                                        <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 16}}>Annuler la
                                            dernière transaction</Text>
                                    </TouchableOpacity>
                                    }
                                </View>
                            </View>
                            { showErr && <Text style={styles.textError}>La dernière transaction a déjà été annulée.</Text>}
                        </View>
                    </View>
                }
            </View>
        )
    }
}

const styles = StyleSheet.create({
    GridView: {
        alignItems: 'center',
        justifyContent: 'flex-start',
        flex:1,
        height: 70,
        elevation : 0,
        flexDirection:'row'
    },
    inputGrid: {
        flex:1,
        width:90, maxHeight:60,
        borderRadius:6,
        backgroundColor: '#fff',
        alignItems: 'center',
        justifyContent: 'center',
        elevation: 2
    },
    inputGridSelected: {
        flex:1,
        width:90, maxHeight:60,
        borderRadius:6,
        backgroundColor: '#bdbdbd',
        alignItems: 'center',
        justifyContent: 'center',
        elevation: 2
    },
    textInput: {
        fontSize: 16,
        fontFamily:'Livvic-Regular'
    },
    textInputSelected: {
        fontSize: 16,
        fontFamily:'Livvic-Regular',
        color:'#fff'
    },
    headerTab: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        height: '10%',
        padding: 6,
        width:"90%"
    },
    headerText: {
        color: '#bdbdbd',
        fontSize: 16,
        fontFamily: 'Livvic-Regular'
    },
    textContent: {
        textAlign:'center',
        fontFamily: 'Livvic-Regular',
        fontSize: 12,
    },
    textError: {
        textAlign: 'center',
        color: '#e53935',
        fontSize: 16,
        fontFamily:'Livvic-Regular',
        marginTop:10
    },
    tabs: {
        flexDirection: 'row',
    },
    tabTextStyle: {
        marginLeft: 5,
        marginRight: 5,
        fontSize: 20,
    },
    tabUnderline: {
        textDecorationLine: 'underline',
    },
    buttonsContainer: {
        flex:1, justifyContent:'center',  alignItems:'center'
    },
    buttonStyle: {
        padding: 10,
        borderRadius: 5,
        marginBottom: 10,
        borderColor: '#000000',
        borderWidth: 1,
    },
});

const mapStateToProps = (state) => {
    return {
        ticket : state.toggleTicket.ticket,
        expiredAt : state.toggleTicket.expiredAt,
        token : state.toggleLogin.token
    }
};

export default connect(mapStateToProps)(listTransactions);

