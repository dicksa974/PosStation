import React, { Component } from 'react';
import {View, Text, Dimensions, TouchableOpacity, Image, FlatList, StyleSheet, ActivityIndicator, NativeModules} from 'react-native';
import { Button, Icon } from 'react-native-elements';
import { host } from "../../utils/constants";
import { connect } from "react-redux";
import Modal from "react-native-modalbox";
import FontAwesome5 from "react-native-vector-icons/FontAwesome5";

const { width, height } = Dimensions.get('window');

const arrayOfNumbers = [
    { key: 1 },
    { key: 2 },
    { key: 3 },
    { key: 4 },
    { key: 5 },
    { key: 6 },
    { key: 7 },
    { key: 8 },
    { key: 9 },
    { key: 10 },
    { key: 0 },
    { key: 12 }
];

const empties = [
    { key: 1, value: ' ' },
    { key: 2, value: ' ' },
    { key: 3, value: ' ' },
    { key: 4, value: ' ' },
    { key: 5, value: ' ' },
    { key: 6, value: ' ' }
];

let counter = 0;

const activityStarter = NativeModules.ActivityStarter;

class saisieRechargeCarte extends React.Component {

    static navigationOptions = ({ navigation, screensProps }) => ({
        headerLeft: <Icon type={'simple-line-icon'} name={'arrow-left'} color={'black'} onPress={ () => { navigation.goBack() }}/>,
        headerRight:<Icon type={'simple-line-icon'} name={'home'} color={'#03498e'} onPress={ () => { navigation.navigate("Home") }} containerStyle={{marginRight:20}} />,
        headerTitle:
          <View style={{flex:1, flexDirection:'row'}}>
              <Image source={require('../../../assets/images/iconx96.jpg')} style={{width:55, height:55, borderRadius:8, marginTop:10}} resizeMode={"contain"}/>
              <Text style={{color:'#03498e', fontSize:20, marginLeft:15, fontFamily:'Livvic-Medium', textAlignVertical:'center'}}>SAISIE DE CARTE</Text>
          </View>,
      headerTransparent: true
    });

    constructor(props) {
        super(props);
        this.state = {
            loading: false,
            item: {},
            showError: false,
            showErrorType: false
        };
    }

    state = {
        code: '',
        digitDisabled: false,
        clearDisabled: false
    };

    async _getCarteUuid(uuid) {
        // this.setState({loading: true});

         this._cleanInput();

        fetch(host + "/cartes/uuid/"+uuid, {
            method: "GET",
            headers: {
                Accept : "application/json",
                "Content-Type" : "application/json",
                Authorization: "Bearer " + this.props.token
            }
        }).then((response) => {
            console.log("recharge carte",response);
            if(response.status === 200) {
                activityStarter.showPinPadText('Bienvenu');
                response.json().then(data => {
                    console.log(data);
                    if(data.typePayement === "CARTE_PRE_PAYEE" && data.status === "ACTIVE"){
                        this.setState({ loading: false, carte: data, showErrorType: false, showError: false });
                        this.props.navigation.navigate('SaisieMontant', {carte: data});
                    }
                    else {
                        this.setState({ showErrorType: true, loading: false });
                    }

                }).catch(error =>  this.setState({ showError: true, loading: false }));
            }
            else {
                activityStarter.showPinPadText('Carte non valide');
                this.setState({ showError: true, loading: false })
            }
        }).catch(error =>  this.setState({ showError: true, loading: false }))
    };

    async _getCarte () {
        // this.setState({loading: true});
         let carte="";
         empties.forEach(function(element) {
             carte = carte.concat(element.value);
         });
        // console.log(this.state.item);
         let strCarte= carte.split(' ').join('');
         this._cleanInput();

        fetch(host + "/cartes/sn/"+strCarte, {
            method: "GET",
            headers: {
                Accept : "application/json",
                "Content-Type" : "application/json",
                Authorization: "Bearer " + this.props.token
            }
        }).then((response) => {
            console.log("recharge carte",response);
            if(response.status === 200) {
                response.json().then(data => {
                    console.log(data);
                    if(data.typePayement === "CARTE_PRE_PAYEE" && data.status === "ACTIVE"){
                        this.setState({ loading: false, carte: data, showErrorType: false, showError: false });
                        this.props.navigation.navigate('SaisieMontant', {carte: data});
                    }
                    else {
                        this.setState({ showErrorType: true, loading: false });
                    }

                }).catch(error =>  this.setState({ showError: true, loading: false }));
            }
            else {
                this.setState({ showError: true, loading: false })
            }
        }).catch(error =>  this.setState({ showError: true, loading: false }))
    };

    onEnterDigit = (num, index) => {
        const { code } = this.state;
        if (counter + 1 <= 6) {
            counter++;
            empties[counter - 1].value = num;
            this.setState({
                clearDisabled: false
            });
        }
        if (counter === 6) {
            let carte="";
            empties.forEach(function(element) {
                carte = carte.concat(element.value);
            });
            console.log(this.state.item);
            let item = {...this.state.item, carte: carte};
            console.log(item);
            //this.props.navigation.navigate('SaisiePinCode', {item: item});
            //this.setState({digitDisabled: true});
        }
    };

    _cleanInput = () => {
        console.log('clean');
        while (counter > 0) {
            --counter;
            empties[counter].value = ' ';
            this.setState({
                digitDisabled: false,
                showError : false,
                showErrorType : false
            });
        }
    };

    onRemoveDigit = () => {
        if (counter - 1 >= 0) {
            --counter;
            empties[counter].value = ' ';
            this.setState({
                digitDisabled: false,
                showError : false,
                showErrorType : false
            });
        } else {
            this.setState({
                allowClear: true,
                showError : false,
                showErrorType : false
            });
        }
    };

    renderItemCell = ({ item, index }) => {
        const { withTouchId = false } = this.props;
        if (index === 9) {
            if(withTouchId) {
                return (
                    <View style={[styles.round]} />
                );
            }else{
                return <View style={[styles.round]} />;
            }

        } else if (index === 11) {
            return (
                <View style={[styles.round]} />
            );
        } else {
            return (
                <TouchableOpacity
                    style={[styles.round, styles.centerAlignment]}
                    onPress={() => this.onEnterDigit(item.key)}
                    disabled={this.state.digitDisabled}
                >
                    <Text style={styles.digit}>{item.key}</Text>
                </TouchableOpacity>
            );
        }
    };

    render() {
        const { spaceColor } = this.props;
        const { showError, loading, showErrorType } = this.state;

        return(
            <View style={{flex:1, backgroundColor:'#fafafa'}}>
                <View style={{width:"94%", height:1, backgroundColor: "#bdbdbd", marginLeft:'3%', marginTop:'6%'}}/>
                {loading &&
                <View style={{flex:1, justifyContent:'center', alignItems: 'center'}}>
                    <ActivityIndicator size="large" color={"#03498e"}/>
                </View>
                }
                {!loading &&
                <View style={{flex:1, padding:4, flexDirection:'row'}}>
                    <View style={{flex:3}}>
                        <View style={styles.enterView}>
                            {empties.map(item => (
                                <View key={item.key} style={styles.digitView}>
                                    <Text style={styles.code}>{item.value}</Text>
                                    <View style={[styles.redSpace, { backgroundColor: spaceColor || '#bdbdbd'}]} />
                                </View>
                            ))}
                        </View>
                        <View style={[styles.textView, styles.centerAlignment]}>
                            { showError && <Text style={styles.textError}>Cette carte est périmée ou n&apos;existe pas ! </Text>}
                            { showErrorType && <Text style={styles.textError}>Recharge non autorisée ! </Text>}
                            <Text style={styles.instruction}>Veuillez saisir le numéro de carte</Text>
                        </View>
                        <View style={styles.flatcontainer}>
                            <FlatList
                                style={styles.flatlist}
                                data={arrayOfNumbers}
                                renderItem={this.renderItemCell}
                                numColumns={3}
                            />
                        </View>
                        </View>
                    <View style={{flex:1, justifyContent:'center'}}>
                            <View style={{width:"80%", height:85}}>
                            <Button
                                icon={{
                                    name: "times",
                                    size: 20,
                                    color: "white",
                                    type: "font-awesome"
                                }}
                                title="Annuler"
                                buttonStyle={{backgroundColor:'#e53935'}}
                                onPress={this._cleanInput}
                            />
                            </View>
                            <View style={{width:"80%", height:85}}>
                            <Button
                                icon={{
                                    name: "chevron-left",
                                    size: 20,
                                    color: "white",
                                    type: "font-awesome"
                                }}
                                title="Corriger"
                                buttonStyle={{backgroundColor:'#ffc107'}}
                                onPress={this.onRemoveDigit}
                            />
                            </View>
                        <View style={{width:"80%", height:85}}>
                            <Button
                              icon={{
                                  name: "credit-card",
                                  size: 20,
                                  color: "white",
                                  type: "font-awesome"
                              }}
                              title="Sans contact"
                              buttonStyle={{backgroundColor:'#9e9e9e'}}
                              onPress={() => activityStarter.FuncM1((carte) => {this._getCarteUuid(carte)})}
                            />
                        </View>
                            <View style={{width:"80%", height:85}}>
                            <Button
                                icon={{
                                    name: "check",
                                    size: 20,
                                    color: "white",
                                    type: "font-awesome"
                                }}
                                title="Valider"
                                buttonStyle={{backgroundColor:'#4caf50'}}
                                onPress={() => {this._getCarte()}}
                            />
                            </View>
                        </View>
                </View>
                }
            </View>
        )
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1
    },
    centerAlignment: {
        justifyContent: 'center',
        alignItems: 'center'
    },
    enterView: {
        alignSelf: 'center',
        marginBottom: 10,
        flexDirection: 'row',
        flex: 1,
        maxHeight:55,
        justifyContent: 'flex-end',
        alignItems: 'center'
    },
    flatcontainer: {
        flex: 3,
        alignItems:'center',
        justifyContent:'flex-end'
    },
    flatlist: {
        alignSelf: 'center'
    },
    icon: {
        height: 24,
        width: 24
    },
    round: {
        width: 75,
        height: 75,
        backgroundColor: '#03498e',
        borderRadius: 35,
        margin: 10
    },
    instruction: {
        marginHorizontal: 30,
        textAlign: 'center',
        color: '#757575',
        fontSize: 16,
        fontFamily:'Livvic-Regular'
    },
    textError: {
        marginHorizontal: 30,
        textAlign: 'center',
        color: '#e53935',
        fontSize: 16,
        fontFamily:'Livvic-Regular'
    },
    close: {
        marginTop: 30,
        marginLeft: 15
    },
    digit: {
        fontSize: 24,
        color:'#fff'
    },
    code: {
        fontSize: 24,
        color:'#03498e'
    },
    digitView: {
        flexDirection: 'column',
        alignItems: 'center'
    },
    redSpace: {
        height: 2,
        width: 40,
        marginHorizontal: 5
    },
    textView: {
        flex: 0.5,
        marginBottom: 10
    },
    deleteIcon: {
        height: 20,
        width: 20
    }
});

const mapStateToProps = (state) => {
    return {
        token : state.toggleLogin.token
    }
};

export default connect(mapStateToProps)(saisieRechargeCarte);
