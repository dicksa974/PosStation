import React, { Component } from 'react';
import {
    View,
    Text,
    ImageBackground, StyleSheet, Dimensions
} from 'react-native';
import {
    Input,
    Button, Image,
} from 'react-native-elements';
import { connect } from 'react-redux';
import Fontisto from "react-native-vector-icons/Fontisto";
import { KeyboardAwareScrollView } from 'react-native-keyboard-aware-scroll-view';
import { host } from "../utils/constants";
import axios from "axios";
import queryString  from "query-string";

const { width, height } = Dimensions.get('window');

class SignIn extends React.Component {

    static navigationOptions = {
        headerMode: 'none'
    };

    constructor(props) {
        super(props);
        this.state = {
            username : '',
            password : '',
            strErr: false,
            loading: false
        };
        this._getLogIn = this._getLogIn.bind(this)
    }

    async _getLogIn () {
        this.setState({loading: true});

      /*  if(this.state.username === "stleu1" && this.state.password === "olacard147#"){
            let station = { num: "101", name: "Saint-Leu", password : "147#"};
            this.props.navigation.navigate('Home', {station: station});
            //SAVE SESSION
            let authorization = station.name;
            const action = { type : "LOGGED_IN", value : authorization};
            this.props.dispatch(action);
        }
        else if(this.state.username === "montvert1" && this.state.password === "olacard775#") {
            let station = { num: "100", name: "MONVERT LINA", password : "775#"};
            this.props.navigation.navigate('Home', {station: station});

            //SAVE SESSION
            let authorization = station.name;
            const action = { type : "LOGGED_IN", value : authorization};
            this.props.dispatch(action);
        }
        else if(this.state.username === "terresainte1" && this.state.password === "olacard771#") {
            let station = { num: "100", name: "TERRE SAINTE", password : "771#"};
            this.props.navigation.navigate('Home', {station: station});

            //SAVE SESSION
            let authorization = station.name;
            const action = { type : "LOGGED_IN", value : authorization};
            this.props.dispatch(action);
        }
        else {
            this.setState({strErr: true, loading: false})
        }*/

        let data = {
            grant_type: "password",
            username: this.state.username,
            password: this.state.password,
            client_id: "browser",
            client_secret: "1234",
            redirect_url: host + "/uaa/oauth/token"
        };
        console.log(host + "/uaa/oauth/token");
        console.log(queryString.stringify(data));
        axios({
            method: "POST",
            url: host + "/uaa/oauth/token",
            baseUrl: host,
            headers: {
                "Access-Control-Allow-Origin": "*",
                "content-type": "application/x-www-form-urlencoded"
            },

            data: queryString.stringify(data)
        })
            .then((response) => {
             console.log(response);
             if(response.status === 200) {
                 let token = response.data.access_token;
                 const action = { type : "LOGGED_IN", value : token};
                 this.props.dispatch(action);
                 this.setState({loading: false});
                 this.props.navigation.navigate('Home');
             }
             else {
                 this.setState({strErr: true, loading: false})
             }
         })
             .catch(error => {
                 console.log(error);
                 this.setState({strErr: true, loading: false})})
    };

    render() {
        const { strErr, loading } = this.state;
        return (
            <ImageBackground style={styles.imageContainer} source={require('../../assets/images/bgPay.jpg')} resizeMode="cover">
                <KeyboardAwareScrollView style={styles.contentImage}>
                    <View style={{flex:1, justifyContent:'space-evenly', alignItems: 'center', height:height}}>
                        <Image source={require('../../assets/images/logo.png')} style={{width: 200, height: 170, resizeMode: 'contain'}}/>

                        <View style={{flex:1, width:450, maxHeight:240, backgroundColor:'#fff', borderRadius:6, justifyContent:'space-between', alignItems: 'center'}}>
                            <View style={{flex:1,width:'95%',  justifyContent:'center'}}>
                            <Input
                                label={'Identifiant'}
                                placeholder='Votre identifiant'
                                leftIcon={
                                    <Fontisto
                                        name='male'
                                        size={24}
                                        color='#9e9e9e'
                                        style={{marginRight:10}}
                                    />
                                }
                                style={{marginTop:10, marginLeft:10}}
                                autoCapitalize = 'none'
                                onChangeText={(value) => this.setState({username: value})}
                                value={this.state.username}
                            />
                            </View>
                            <View style={{flex:1, width:'95%', justifyContent:'center'}}>
                            <Input
                                label={'Mot de passe'}
                                placeholder='Votre mot de passe'
                                leftIcon={
                                    <Fontisto
                                        name='locked'
                                        size={24}
                                        color='#9e9e9e'
                                        style={{marginRight:10}}
                                    />
                                }
                                style={{marginLeft:10}}
                                autoCapitalize = 'none'
                                secureTextEntry={true}
                                onChangeText={(value) => this.setState({password: value})}
                                value={this.state.password}
                            />
                            </View>
                            { strErr && <Text style={styles.textError}>Une erreur est survenue ! Veuillez réessayer </Text>}
                            <View style={{flex:1, width:'100%', justifyContent:'center'}}>
                            <Button
                                title="Valider"
                                onPress={this._getLogIn}
                                type={"clear"}
                                titleStyle={{color:"#03498e"}}
                                loading={loading}
                            />
                            </View>
                        </View>
                    </View>

                </KeyboardAwareScrollView>
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
        height: "100%",
        width: "100%",
        //alignItems: 'center'
    },
    textError: {
        marginHorizontal: 30,
        textAlign: 'center',
        color: '#e53935',
        fontSize: 16,
        fontFamily:'Livvic-Regular'
    },
});

const mapStateToProps = (state) => {
  return {
      loggedIn : state.toggleLogin.loggedIn
  }
};

export default connect(mapStateToProps)(SignIn);
