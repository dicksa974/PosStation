import React from 'react';
import {View, StyleSheet, ImageBackground, Dimensions, TouchableOpacity, Text, ActivityIndicator} from 'react-native';
import Fontisto from 'react-native-vector-icons/Fontisto';
import FontAwesome5 from 'react-native-vector-icons/FontAwesome5';
import { Image } from "react-native-elements";
import { connect } from "react-redux";


let deviceWidth = Dimensions.get("window").width;
let deviceHeight = Dimensions.get("window").height;

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

    render() {
        return(
            <ImageBackground style={styles.imageContainer} source={require('../../assets/images/bgPay.jpg')} resizeMode="cover">
                <View style={styles.contentImage}>
                    <View style={{flex:1,width:'100%', maxHeight:60, justifyContent:'flex-end', alignItems: 'flex-end'}}>
                        <Fontisto name={"power"} color="#fff" size={40} style={{marginTop:5, marginRight:10}} onPress={ () => { this._signOut() }}/>
                    </View>
                    <View style={{flex:1, justifyContent:'center', alignItems: 'center'}}>
                        <Image source={require('../../assets/images/logo.png')} style={{width: 200, height: 170, resizeMode: 'contain'}}/>
                    </View>
                    <View style={{flex:1, flexDirection:'row', justifyContent:'center'}}>
                        <View style={{flex:1, justifyContent:'flex-start', alignItems: 'center'}}>
                            <TouchableOpacity onPress={() => {this.props.navigation.navigate('SaisieCarte', {station: this.state.station});}} style={{justifyContent:'center', alignItems: 'center'}}>
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

