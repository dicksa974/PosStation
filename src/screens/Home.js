import React from 'react';
import {
    View,
    StyleSheet,
    ImageBackground,
    Dimensions,
    TouchableOpacity,
    Text,
    ActivityIndicator,
    NativeModules
} from 'react-native';
import Fontisto from 'react-native-vector-icons/Fontisto';
import FontAwesome5 from 'react-native-vector-icons/FontAwesome5';
import {Image, Input} from "react-native-elements";
import { connect } from "react-redux";
import {host} from "../utils/constants";
import Modal from "react-native-modalbox";


let deviceWidth = Dimensions.get("window").width;
let deviceHeight = Dimensions.get("window").height;

const activityStarter = NativeModules.ActivityStarter;

class Home extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            station: {},
            carte: {},
            pincode: "",
            showPinErr: false,
            loadingPay: false
        }
    }

    async componentDidMount() {
        const { navigation } = this.props;
        const s = navigation.getParam('station', {});
        this.setState({station: s});
    }

    _signOut() {
        const action = { type: "LOGGED_OUT"};
        this.props.dispatch(action);
        this.props.navigation.navigate("Auth");
    }

    async _getCarteUuid(uuid) {
        fetch(host + "/cartes/uuid/"+uuid, {
            method: "GET",
            headers: {
                Accept : "application/json",
                "Content-Type" : "application/json",
                Authorization: "Bearer " + this.props.token
            }
        }).then((response) => {
            if(response.status === 200) {
                activityStarter.showPinPadText("Bienvenue");
                response.json().then(data => {
                    this.setState({ carte:data });
                    this.refs.modalLoad.close();
                    this._redirect();
                }).catch(error => {
                    this.refs.modalLoad.close();
                    this.setState({showError: true})
                })
            }
            else {
                activityStarter.showPinPadText('Carte non valide');
                this.refs.modalLoad.close();
                this.setState({ showError: true })
            }
        }).catch(error => {
            console.log(error);
            this.refs.modalLoad.close();
            this.setState({ showError: true})
        })
    };

    _openModal(page){
        this.setState({page: page});
        this.refs.modalLoad.open();
    }

    _activeCarte(){
        if(this.state.pincode === "1234") {
            if(this.state.carte.status === "PREACTIVE") {
                this.setState({showPinErr: false, loadingPay: true});
                let ticket = {
                    carte: {id: this.state.carte.id},
                    station: {id: "5df8f9bb261cf3202ab9a13e"},
                    transactions: [{montant: 0}],
                    typeTicket: "ACTIVATION"
                };
                console.log("Activation carte ", ticket);
                fetch(host + "/tickets/active", {
                    method: "POST",
                    headers: {
                        Accept: "application/json",
                        "Content-Type": "application/json",
                        Authorization: "Bearer " + this.props.token
                    },
                    body: JSON.stringify(ticket)
                }).then((res) => {
                    console.log("response TICKET", res);
                    this.setState({loadingPay: false});
                    if (res.status === 200) {
                        fetch(host + "/cartes/status/ACTIVE/" + this.state.carte.id, {
                            method: "PUT",
                            headers: {
                                Accept: "application/json",
                                "Content-Type": "application/json",
                                Authorization: "Bearer " + this.props.token
                            }
                        })
                            .then((res) => {
                                console.log("response carte", res);
                                this.setState({pincode: "", loadingPay: false});
                                this.refs.modalConfirmActive.close();
                                this.refs.modalSuccessActive.open();
                            })
                            .catch((err) => {
                                    console.log("response carte", err);
                                this.setState({pincode: "", loadingPay: false});
                                    this.refs.modalConfirmActive.close();
                                    this.refs.modalFailActive.open();
                                }
                            );
                    } else {
                        this.setState({pincode: "", loadingPay: false});
                        this.refs.modalConfirmActive.close();
                        this.refs.modalFailActive.open();
                    }
                }).catch(error => {
                    this.setState({loadingPay: false, pincode: ""});
                    this.refs.modalConfirmActive.close();
                    this.refs.modalFailActive.open();
                });
            }
            else {
                this.setState({pincode: "", loadingPay: false});
                this.refs.modalConfirmActive.close();
                this.refs.modalFailActive.open();
            }
        }
        else {
            this.setState({showPinErr: true, loadingPay: false})
        }
    }

    _redirect(){
        let carte = this.state.carte;

        switch (this.state.page) {
            case "INFO":
                this.props.navigation.navigate('InfoUser', {item: carte});
                break;
            case "RECHARGE":
                if(carte.typePayement === "CARTE_PRE_PAYEE" && carte.status === "ACTIVE" ){
                    this.props.navigation.navigate('SaisieMontant', {carte: carte});
                }
                else {
                    this.refs.modalFailRecharge.open();
                }
                break;
            case "ACTIVATION":
                this.refs.modalConfirmActive.open();
                break;
        }
    }

    render() {
        const { showPinErr, loadingPay } = this.state;

        return(
            <ImageBackground style={styles.imageContainer} source={require('../../assets/images/bgPay.jpg')} resizeMode="cover">
                <View style={styles.contentImage}>
                    <Modal style={{  height: 250, width: 400, backgroundColor:'#fff', borderRadius:4, padding:5 }} ref={"modalLoad"} onOpened={()=> { activityStarter.AutoCard((carte) => {this._getCarteUuid(carte)})}} swipeToClose={false} backdropPressToClose={false}>
                        <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"spinner"} color={"green"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Scan en cours ...</Text>
                            </View>
                        </View>
                    </Modal>
                    <Modal style={{  height: 250, width: 400, backgroundColor:'#fff', borderRadius:4, padding:5 }} ref={"modalFailRecharge"} swipeToClose={false} backdropPressToClose={false}>
                        <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"check-circle"} color={"#e53935"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Une erreur est survenue !</Text>
                            </View>
                            <View style={{flex:2, justifyContent:'center', alignItems:'center', marginTop:20}}>
                                <View style={{flex:1, marginTop:15 }}>
                                    <Text style={{fontFamily: 'Livvic-Regular', color: '#757575', fontSize: 17, textAlign: 'center'}}>
                                        Cette carte ne peut pas être rechargée. </Text>
                                </View>
                            </View>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <TouchableOpacity style={{width: 140, height: 50, backgroundColor:'#e53935', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                                  onPress={() => {this.refs.modalFailRecharge.close()}}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Retour</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </Modal>
                    <Modal style={{ height: 350, width: 450, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalConfirmActive"} swipeToClose={false} backdropPressToClose={false} backdrop={true} >
                        <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"question-circle"} color={"#fb8c00"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Activation Carte ?</Text>
                                {loadingPay &&
                                    <View style={{flex: 1, backgroundColor: '#fafafa', justifyContent:'center', alignItems:'center'}}>
                                        <ActivityIndicator color={'blue'} size={"large"}/>
                                    </View>
                                }
                                {!loadingPay &&
                                    <View style={{flex: 1, marginTop: 15}}>
                                        <Text style={{
                                            fontFamily: 'Livvic-Regular',
                                            color: '#757575',
                                            fontSize: 17,
                                            textAlign: 'center',
                                            marginBottom: 15
                                        }}>
                                            Confirmer l'activation de la carte n°{this.state.carte.serialNumber} ? </Text>
                                        <View style={{flex: 1, width: 350, justifyContent: 'center', marginTop: 10}}>
                                            <Input label={'Pin Code Caisse'} keyboardType={"number-pad"}
                                                   leftIcon={
                                                       <Fontisto name='locked' size={20} color='#9e9e9e'
                                                                 style={{marginRight: 10}}/>
                                                   }
                                                   autoFocus={true}
                                                   style={{marginTop: 20, marginLeft: 10}}
                                                   onChangeText={(value) => this.setState({pincode: value})}
                                                   value={`${this.state.pincode}`}
                                            />
                                            {showPinErr && <Text style={styles.textError}> Erreur Pin Code </Text>}
                                        </View>
                                    </View>
                                }
                            </View>

                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <TouchableOpacity style={{width: 125, height: 45, marginTop:15, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                                  onPress={() => {this._activeCarte();}}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Valider</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </Modal>
                    <Modal style={{ height: 250, width: 400, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalSuccessActive"} swipeToClose={false} backdropPressToClose={false} backdrop={true} >
                        <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"check-circle"} color={"green"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Activation Enregistrée !</Text>
                                <View style={{flex:1, marginTop:15 }}>
                                    <Text style={{fontFamily: 'Livvic-Regular', color: '#757575', fontSize: 17, textAlign: 'center'}}>
                                        La carte n°{this.state.carte.serialNumber} est activée </Text>
                                </View>
                            </View>

                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <TouchableOpacity style={{width: 140, height: 50, marginTop:15, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                                  onPress={() => {this.refs.modalSuccessActive.close()}}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Valider</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </Modal>
                    <Modal style={{ height: 350, width: 450, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalFailActive"} swipeToClose={false} backdropPressToClose={false} backdrop={true} >
                        <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"check-circle"} color={"#e53935"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Activation Echouée !</Text>
                                <View style={{flex:1, marginTop:15 }}>
                                    <Text style={{fontFamily: 'Livvic-Regular', color: '#757575', fontSize: 17, textAlign: 'center'}}>
                                        L'activation de la carte n°{this.state.carte.serialNumber} est échouée.</Text>
                                    <Text style={{fontFamily: 'Livvic-Regular', color: '#757575', fontSize: 17, textAlign: 'center'}}>
                                        Cette carte ne peut pas être activée.</Text>
                                </View>
                            </View>

                            <View style={{flex:1, justifyContent:'center', alignItems:'center',  marginTop:5}}>
                                <TouchableOpacity style={{width: 140, height: 50, marginTop:15, backgroundColor:'red', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                                  onPress={() => {this.refs.modalFailActive.close()}}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Retour</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </Modal>
                    <View style={{flex:1,width:'100%', maxHeight:60, justifyContent:'flex-end', alignItems: 'flex-end'}}>
                        <Fontisto name={"power"} color="#fff" size={40} style={{marginTop:5, marginRight:10}} onPress={ () => { this._signOut() }}/>
                    </View>
                    <View style={{flex:1, justifyContent:'center', alignItems: 'center'}}>
                        <Image source={require('../../assets/images/logo.png')} style={{width: 200, height: 170, resizeMode: 'contain'}}/>
                    </View>
                    <View style={{flex:1, flexDirection:'row', justifyContent:'center'}}>
                        <View style={{flex:1, justifyContent:'flex-start', alignItems: 'center'}}>
                            <TouchableOpacity onPress={() => {this._openModal("INFO")}} style={{justifyContent:'center', alignItems: 'center'}}>
                                <FontAwesome5 name={"cash-register"} color="#fff" size={60}/>
                                <Text style={{color:'#fff', fontSize:20, marginTop:10, fontFamily:'Livvic-SemiBold'}}>Caisse</Text>
                            </TouchableOpacity>
                        </View>

                        <View style={{flex:1, justifyContent:'flex-start', alignItems: 'center'}}>
                            <TouchableOpacity onPress={() => {this._openModal("RECHARGE")}} style={{justifyContent:'center', alignItems: 'center'}}>
                                <FontAwesome5 name={"euro-sign"} color="#fff" size={60}/>
                                <Text style={{color:'#fff', fontSize:20, marginTop:10, fontFamily:'Livvic-SemiBold'}}>Recharge</Text>
                            </TouchableOpacity>
                        </View>

                        <View style={{flex:1, justifyContent:'flex-start', alignItems: 'center'}}>
                            <TouchableOpacity onPress={() => {this._openModal("ACTIVATION")}} style={{justifyContent:'center', alignItems: 'center'}}>
                                <FontAwesome5 name={"id-card-alt"} color="#fff" size={60}/>
                                <Text style={{color:'#fff', fontSize:20, marginTop:10, fontFamily:'Livvic-SemiBold'}}>Activation</Text>
                            </TouchableOpacity>
                        </View>

                        <View style={{flex:1, justifyContent:'flex-start', alignItems: 'center'}}>
                            <TouchableOpacity onPress={() => {this.props.navigation.navigate('ListTransactions', {station: this.state.station})}} style={{justifyContent:'center', alignItems: 'center'}}>
                                <Fontisto name={"list-2"} color="#fff" size={60}/>
                                <Text style={{color:'#fff', fontSize:20, marginTop:10, fontFamily:'Livvic-SemiBold'}}>Historique</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </View>
            </ImageBackground>
        )
    }
}

const styles = StyleSheet.create({
    fullContainer: {
        flex: 1,
    },
    imageContainer:{
        width: null,
        height: null,
        alignSelf: 'stretch',
        flex: 1,
        justifyContent: 'center',
    },
    contentImage: {
        backgroundColor: 'rgba(3,73,142, 0.9)',
        height: deviceHeight * 100 / 100,
        width: deviceWidth * 100 / 100,
        justifyContent: 'center',
        alignItems: 'center',
    },
});

const mapStateToProps = (state) => {
    return {
        loggedIn : state.toggleLogin.loggedIn,
        token : state.toggleLogin.token
    }
};

export default connect(mapStateToProps)(Home);

