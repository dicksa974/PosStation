import React from 'react';
import {
    View,
    StyleSheet,
    ImageBackground,
    Dimensions,
    TouchableOpacity,
    Image,
    Text,
    ActivityIndicator
} from 'react-native';
import CalculatorResponse from '../../components/calculator/CalculatorResponse';
import CalculatorButtonsContainer from '../../components/calculator/CalculatorButtonsContainer';
import FontAwesome5 from 'react-native-vector-icons/FontAwesome5';
import Modal from "react-native-modalbox";
import { Input, Icon } from "react-native-elements";
import _ from 'lodash';
import {host} from "../../utils/constants";
import {connect} from "react-redux";
import Fontisto from "react-native-vector-icons/Fontisto";

const { width, height } = Dimensions.get('window');

class saisieMontant extends React.Component {

    static navigationOptions = ({ navigation, screensProps }) => ({
        headerLeft: <Icon type={'simple-line-icon'} name={'arrow-left'} color={'black'} onPress={ () => { navigation.goBack() }}/>,
        headerTitle:
            <View style={{flex:1, flexDirection:'row'}}>
                <Image source={require('../../../assets/images/iconx96.jpg')} style={{width:55, height:55, borderRadius:8, marginTop:10}} resizeMode={"contain"}/>
                <Text style={{color:'#03498e', fontSize:20, marginLeft:15, fontFamily:'Livvic-Medium', textAlignVertical:'center', marginTop:10}}>RECHARGER UNE CARTE</Text>
            </View>,
        headerRight: <Icon type={'simple-line-icon'} name={'home'} color={'#03498e'} onPress={ () => { navigation.navigate("Home") }} containerStyle={{marginRight:20}} />,
        headerTransparent: true
    });


    constructor(props) {
        super(props);
        this.state = {
            first: '0',
            second: '',
            operator: '',
            result: 0,
            total:0,
            carte: "",
            isResult: false,
            showErrMontant:false,
            showPinErr:false,
            pincode:""
        };

        this.refresh = this.refresh.bind(this);
        this.handleButtonPress = this.handleButtonPress.bind(this);
        this.getResult = this.getResult.bind(this);
    }

    async componentDidMount() {
        const { navigation } = this.props;
        const c = navigation.getParam('carte', {});
        this.setState({carte: c});
    }

    getResult(button) {
        const { first, second, operator } = this.state;
        console.log('get  Result', button);
        const parsedFirst = parseFloat(first);
        const parsedSecond = parseFloat(second) || 0;
        let result = 0;

        switch (button) {
            case 'Valider':
                if(parseFloat(first) <= 9999) {
                    this.setState({total: parseFloat(first)});
                    this.refs.modalAsk.open();
                }
                else {
                    this.setState({showErrMontant: true});
                }
                //this.props.navigation.navigate('ScanQrCode', {item: {type: 'recharge', montant: parsedFirst}});
                break;
            /*            case '+':
                            result = parsedFirst + parsedSecond;
                            break;
                        case '−':
                            result = parsedFirst - parsedSecond;
                            break;
                        case '×':
                            result = parsedFirst * parsedSecond;
                            break;
                        case '÷':
                            if (!parsedSecond || parsedSecond === 0) {
                                result = 'Error';
                            } else {
                                result = parseFloat(parsedFirst / parsedSecond).toFixed(8);
                            }
                            break;*/
            default:
                console.log('wrong operator');
        }

        this.setState({
            result,
            isResult: true,
        });
    }

    refresh() {
        this.setState({
            first: '0',
            second: '',
            operator: '',
            result: 0,
        });
    }

    handleButtonPress(button) {
        const { isResult } = this.state;
        let { first, second, operator } = this.state;

        switch (button) {
            case '0':
                if (!isResult) {
                    if (!operator) {
                        if (first[0] !== '0' || first.length !== 1) {
                            first += '0';
                        }
                    } else if (second[0] !== '0' || second.length !== 1) {
                        second += '0';
                    } else {
                        second = '0';
                    }

                    this.setState({ first, second, operator });
                } else {
                    this.setState({
                        first: '0',
                        second: '',
                        operator: '',
                        result: 0,
                        isResult: false,
                    });
                }

                break;
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                if (!isResult) {
                    if (!operator) {
                        if (first[0] === '0' && first.length === 1) {
                            first = button;
                        } else {
                            first += button;
                        }
                    } else if (second[0] === '0' && second.length === 1) {
                        second = button;
                    } else {
                        second += button;
                    }

                    this.setState({ first, second, operator });
                } else {
                    this.setState({
                        first: button,
                        second: '',
                        operator: '',
                        result: 0,
                        isResult: false,
                    });
                }

                break;
            case '.':
                if (!operator) {
                    if (!first.includes('.')) {
                        first += button;
                    }
                } else if (!second.includes('.')) {
                    second += button;
                }

                this.setState({ first, second, operator });

                break;
            case '+':
            case '−':
            case '×':
            case '÷':
                if (!operator) {
                    operator = button;

                    this.setState({ first, second, operator });
                } else {
                    this.getResult();
                }
                break;
            case 'C':
                this.setState({showErrMontant: false});
                this.refresh();
                break;
            case 'Valider':
                this.getResult(button);
                break;
            default:
            // console.log('wrong operator');
        }
    }

    _goToHome () {
        this.refs.modalConfirm.close();
        this.props.navigation.navigate('Home');
    }

    _rechargeCarte() {
        if(this.state.pincode === "1234") {
            this.setState({showPinErr: false});

            let ticket = {
                carte: {id: this.state.carte.id},
                transactions: [{montant: this.state.total}],
                station: {id: "5ddfa08ecf4de44d374f313f"},
                typeTicket: "RECHARGE"
            };
            console.log(JSON.stringify(ticket));

            fetch(host + "/tickets/charge", {
                method: "POST",
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + this.props.token
                },
                body: JSON.stringify(ticket)
            }).then((response) => {
                console.log(response);
                if (response.status === 200) {
                    this.refs.modalAsk.close();
                    this.refs.modalConfirm.open();
                } else {
                    this.refs.modalAsk.close();
                    this.refs.modalError.open()
                }
            }).catch(error => {this.refs.modalAsk.close(); this.refs.modalError.open()});
        }else {
            this.setState({showPinErr: true});
        }
    }

    render() {
        const { first, second, operator, result, showErrMontant, showPinErr } = this.state;
        let btn = "Valider";
        return(
            <View style={{flex:1, backgroundColor:'#fafafa'}}>
                <Modal style={{ height: 350, width: 450, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalAsk"} swipeToClose={false} backdropPressToClose={false} backdrop={true} >
                    <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <FontAwesome5 name={"question-circle"} color={"#fb8c00"} size={35} style={{marginTop:15}}/>
                            <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Recharge Carte</Text>
                        </View>
                        <View style={{flex:2, justifyContent:'center', alignItems:'center', marginTop:20}}>
                            <View style={{flex:1, marginTop:15 }}>
                                <Text style={{fontFamily: 'Livvic-Regular', color: '#757575', fontSize: 17, textAlign: 'center'}}>
                                    Confirmer le montant de {this.state.total.toFixed(2).replace('.',',')} € pour</Text>
                                <Text style={{fontFamily: 'Livvic-Regular', color: '#757575', fontSize: 17, textAlign: 'center'}}>
                                    la carte n°{this.state.carte.serialNumber} ?</Text>
                            </View>
                            <View style={{flex: 1, width: 350, justifyContent: 'center'}}>
                                <Input
                                    label={'Pin Code Caisse'}
                                    keyboardType={"number-pad"}
                                    leftIcon={
                                        <Fontisto
                                            name='locked'
                                            size={20}
                                            color='#9e9e9e'
                                            style={{marginRight: 10}}
                                        />
                                    }
                                    autoFocus={true}
                                    style={{marginTop: 20, marginLeft: 10}}
                                    onChangeText={(value) => this.setState({pincode: value})}
                                    value={`${this.state.pincode}`}
                                />
                                {showPinErr && <Text style={styles.textError}> Erreur Pin Code </Text>}
                            </View>
                        </View>
                        <View style={{flex:1, flexDirection:'row', justifyContent:'space-around', alignItems:'flex-end', marginBottom:10, marginTop:5}}>
                            <TouchableOpacity style={{width: 135, height: 45, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => { this._rechargeCarte();} }>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Confirmer</Text>
                            </TouchableOpacity>
                            <TouchableOpacity style={{width: 135, height: 45, marginLeft:20,  backgroundColor:'red', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => { this.refs.modalAsk.close();}}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Annuler</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </Modal>
                <Modal style={{ height: 250, width: 400, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalConfirm"} swipeToClose={false} backdropPressToClose={false} backdrop={true} >
                    <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <FontAwesome5 name={"check-circle"} color={"green"} size={35} style={{marginTop:15}}/>
                            <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Recharge Enregistrée!</Text>
                        </View>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <TouchableOpacity style={{width: 140, height: 50, marginTop:15, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => {this._goToHome()}}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Valider</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </Modal>
                <Modal style={{ height: 250, width: 400, backgroundColor:'#fff', borderRadius:4, padding:5 }} position={"center"} ref={"modalError"} swipeToClose={false} backdropPressToClose={false} backdrop={true} >
                    <View style={{flex: 1, justifyContent:'center', alignItems:'center'}}>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <FontAwesome5 name={"check-circle"} color={"#e53935"} size={35} style={{marginTop:15}}/>
                            <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Une erreur est survenue !</Text>
                        </View>
                        <View style={{flex:2, justifyContent:'center', alignItems:'center', marginTop:20}}>
                            <View style={{flex:1, marginTop:15 }}>
                                <Text style={{fontFamily: 'Livvic-Regular', color: '#757575', fontSize: 17, textAlign: 'center'}}>
                                    La carte n'a pas pu être rechargée. </Text>
                            </View>
                        </View>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <TouchableOpacity style={{width: 140, height: 50, backgroundColor:'#e53935', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                              onPress={() => {this._goToHome()}}>
                                <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Valider</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </Modal>
                <View style={{width:"90%", height:1, backgroundColor: "#bdbdbd", marginLeft:'2%', justifyContent:'center'}}/>
                <View style={{flex:1, height:'90%', width:'40%', marginLeft:'25%', marginTop:'10%'}}>
                    { showErrMontant && <Text style={styles.textError}>Le montant doit être inférieur à 10 000 € ! </Text>}
                    <CalculatorResponse
                        first={first}
                        second={second}
                        operator={operator}
                        result={result}
                        refresh={this.refresh}/>
                    <CalculatorButtonsContainer handleButtonPress={this.handleButtonPress}/>
                    <View>
                        <TouchableOpacity style={{
                            width: 180,
                            height: 50,
                            backgroundColor: 'green',
                            justifyContent: 'center',
                            alignItems: 'center',
                            borderRadius: 5,
                            marginTop: 15,
                            marginLeft:120
                        }} onPress={() => { this.handleButtonPress(btn) }}>
                            <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 14}}>Valider</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            </View>
        )
    }
}

const styles = StyleSheet.create({
    GridView: {
        alignItems: 'center',
        justifyContent: 'flex-start',
        flex:1,
        height: 80,
        elevation : 0,
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
    textError: {
        marginHorizontal: 30,
        textAlign: 'center',
        color: '#e53935',
        fontSize: 16,
        fontFamily:'Livvic-Regular'
    }
});

const mapStateToProps = (state) => {
    return {
        //transaction : state.toggleTransaction.transaction,
        token : state.toggleLogin.token
    }
};

export default connect(mapStateToProps)(saisieMontant);
