import React from 'react';
import { StyleSheet, ImageBackground, View, TouchableOpacity, Text } from 'react-native';
import NetInfo from "@react-native-community/netinfo";
import { connect } from 'react-redux';
import Modal from "react-native-modalbox";
import FontAwesome5 from 'react-native-vector-icons/FontAwesome5';
import moment from 'moment';

class SplashScreen extends React.Component {

    constructor(props) {
        super(props);
    }

    async componentDidMount() {
        const data = await this.performTimeConsumingTask();
        if (data !== null) {
            this._checkReseau();
        }
    }

    _checkReseau() {
        NetInfo.fetch().then(state => {
            if(!state.isConnected){
                this.refs.modal.open();
            }
            else {
                this.refs.modal.close();
                this._checkSession();
            }
        });
    }

    _checkSession() {
        console.log("check session ",this.props);
        if(this.props.loggedIn) {
            let now = moment();
            console.log(this.props.expiredAt);
            if(this.props.expiredAt){
                if(now.isSameOrBefore(this.props.expiredAt)) {
                    this.props.navigation.navigate('Home');
                }
                else{
                    const action = { type: "LOGGED_OUT"};
                    this.props.dispatch(action);
                    this.props.navigation.navigate('Auth');
                }
            }
            else{
                const action = { type: "LOGGED_OUT"};
                this.props.dispatch(action);
                this.props.navigation.navigate('Auth');
            }
        }
        else {
            this.props.navigation.navigate('Auth');
        }
    }

    performTimeConsumingTask = async() => {
        return new Promise((resolve) =>
            setTimeout(
                () => { resolve('result') },
                2000
            )
        );
    };

    render() {
        return (
            <ImageBackground style={styles.imageContainer}
                             source={require('../../assets/images/splash.jpg')}
                             resizeMode="cover">
                <Modal style={{  height: 300, width: 400, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modal"} swipeToClose={false} backdropPressToClose={false}>
                    <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <FontAwesome5 name={"exclamation-circle"} color={"#fb8c00"} size={35} style={{marginTop:15}}/>
                            <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Erreur Réseau !</Text>
                        </View>
                        <View style={{flex:1, padding:5, marginTop:20}}>
                            <Text style={{fontFamily: 'Livvic-Regular', color: '#757575', fontSize: 18}}>
                                L'utilisation de l'application necéssite une connexion Internet.
                                Veuillez l'activer dans vos paramètres.</Text>
                        </View>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <TouchableOpacity style={{width: 140, height: 50, marginTop:15, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => {this._checkReseau()}}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Valider</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </Modal>
            </ImageBackground>
        )
    }
}

const styles = StyleSheet.create({
    imageContainer:{
        width: null,
        height: null,
        alignSelf: 'stretch',
        flex: 1,
        justifyContent: 'center',
    },
});

const mapStateToProps = (state) => {
    return {
        loggedIn : state.toggleLogin.loggedIn,
        expiredAt : state.toggleLogin.expiredAt
    }
};

export default connect(mapStateToProps)(SplashScreen);
