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
import { Image } from "react-native-elements";
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
         station: {}
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
        this.setState({loading: true});
        console.log("Mon token", this.props.token);

        fetch(host + "/cartes/uuid/"+uuid, {
            method: "GET",
            headers: {
                Accept : "application/json",
                "Content-Type" : "application/json",
                Authorization: "Bearer " + this.props.token
            }
        }).then((response) => {
            //console.log(response);
            if(response.status === 200) {

                activityStarter.showPinPadText("Bienvenue");
                response.json().then(data => {
                    //console.log(data);
                    this.setState({ loading: false });
                    this.refs.modalLoad.close();
                    this.props.navigation.navigate('InfoUser', {item: data});
                }).catch(error => {
                    this.refs.modalLoad.close();
                    this.setState({showError: true, loading: false})
                })
            }
            else {
                activityStarter.showPinPadText('Carte non valide');
                this.refs.modalLoad.close();
                this.setState({ showError: true, loading: false })
            }
        }).catch(error => {
            console.log(error);
            this.refs.modalLoad.close();
            this.setState({ showError: true, loading: false })
        })
    };

    _scanCarte(){
        this.refs.modalLoad.open();
        activityStarter.AutoCard((carte) => {this._getCarteUuid(carte)});
    }

    render() {
        return(
            <ImageBackground style={styles.imageContainer} source={require('../../assets/images/bgPay.jpg')} resizeMode="cover">
                <View style={styles.contentImage}>
                    <Modal style={{  height: 250, width: 400, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalLoad"} swipeToClose={false} backdropPressToClose={false}>
                        <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"check-circle"} color={"green"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Scan en cours ...</Text>
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
                            <TouchableOpacity onPress={() => {this._scanCarte()}} style={{justifyContent:'center', alignItems: 'center'}}>
                                <FontAwesome5 name={"cash-register"} color="#fff" size={60}/>
                                <Text style={{color:'#fff', fontSize:20, marginTop:10, fontFamily:'Livvic-SemiBold'}}>Caisse</Text>
                            </TouchableOpacity>
                        </View>

                        <View style={{flex:1, justifyContent:'flex-start', alignItems: 'center'}}>
                            <TouchableOpacity onPress={() => {this.props.navigation.navigate('SaisieRechargeCarte');}} style={{justifyContent:'center', alignItems: 'center'}}>
                                <FontAwesome5 name={"euro-sign"} color="#fff" size={60}/>
                                <Text style={{color:'#fff', fontSize:20, marginTop:10, fontFamily:'Livvic-SemiBold'}}>Recharge</Text>
                            </TouchableOpacity>
                        </View>

                        <View style={{flex:1, justifyContent:'flex-start', alignItems: 'center'}}>
                            <TouchableOpacity onPress={() => {this.props.navigation.navigate('SaisieActivationCarte');}} style={{justifyContent:'center', alignItems: 'center'}}>
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
        loggedIn : state.toggleLogin.loggedIn
    }
};

export default connect(mapStateToProps)(Home);

