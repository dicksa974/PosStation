import React from 'react';
import {
  View,
  StyleSheet,
  Dimensions,
  TouchableOpacity,
  FlatList,
  Text,
  Image,
  ActivityIndicator,
  NativeModules,
  ToastAndroid,
  Alert
} from 'react-native';
import CalculatorResponse from '../../components/calculator/CalculatorResponse';
import CalculatorButtonsContainer from '../../components/calculator/CalculatorButtonsContainer';
import FontAwesome5 from 'react-native-vector-icons/FontAwesome5';
import Fontisto from 'react-native-vector-icons/Fontisto';
import * as _ from 'lodash';
import Modal from "react-native-modal";
import {Input, Icon, Button} from "react-native-elements";
import { connect } from "react-redux";
import { host } from "../../utils/constants";

const { width, height } = Dimensions.get('window');

const activityStarter = NativeModules.ActivityStarter;

class newOrder extends React.Component {

    static navigationOptions = ({ navigation, screensProps }) => ({
        headerLeft: <Icon type={'simple-line-icon'} name={'arrow-left'} color={'black'} onPress={ () => { navigation.goBack() }}/>,
        headerRight:<Icon type={'simple-line-icon'} name={'home'} color={'#03498e'} onPress={ () => { navigation.navigate("Home") }} containerStyle={{marginRight:20}} />,
        headerTitle:
          <View style={{flex:1, flexDirection:'row'}}>
              <Image source={require('../../../assets/images/iconx96.jpg')} style={{width:55, height:55, borderRadius:8, marginTop:10}} resizeMode={"contain"}/>
              <Text style={{color:'#03498e', fontSize:20, marginLeft:15, fontFamily:'Livvic-Medium', textAlignVertical:'center'}}>NOUVEAU PAIEMENT</Text>
          </View>,
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
            isResult: false,
            produits: [],
            tickets:[],
            myProduits: [],
            item: {},
            inputKm: "",
            showError:false,
            showSoldeErr: false,
            showErrMontant:false,
            showErrTotal:false,
            showPinErr: false,
            tentativePin:0,
            station: {},
            pincode:"",
            loadingPay: false,
            loadPay: false,
            modalLoad: false,
            modalAsk: false,
            modalSuccess: false,
            modalFail: false,
        };

        this.refresh = this.refresh.bind(this);
        this.handleButtonPress = this.handleButtonPress.bind(this);
        this.getResult = this.getResult.bind(this);
    }

    async componentDidMount() {
        const { navigation } = this.props;
        const i = navigation.getParam('item', {});
        console.log(i);
        let produits =  _.sortBy(i.produits,"code");
        this.setState({item: i, produits:produits});
    }

    componentWillUnmount() {
        console.log("unmount");
        let arr = [...this.state.produits];
        for (let i = 0; i < this.state.produits.length; i++) {
                arr[i].used = false;
                //arr[i].selected = false;
        }
        this.setState({produits: arr});
    }

    getResult(button) {
        const { first, total } = this.state;
        let result = 0;

        switch (button) {
            case 'Valider':
                if(parseFloat(first)+ total <= 9999) {
                    if (first <= 9999) {
                        //let p = {nom: "", montant: first, codeProduit: 0};
                        let p = {montant: first, produit: {}};
                        let item = {id: "1", nom: "", label: "", montant: first, codeProduit: 0};
                        let arr = [...this.state.produits];
                        for (let i = 0; i < this.state.produits.length; i++) {
                            if (this.state.produits[i].selected === true) {
                                item.nom = this.state.produits[i].libelle;
                                item.label = this.state.produits[i].code;
                                item.codeProduit = this.state.produits[i].num;

                                p.produit = {id: this.state.produits[i].id, code: this.state.produits[i].libelle};
                                //p.codeProduit = this.state.produits[i].num;

                                arr[i].used = true;
                                arr[i].selected = false;

                                let array = [...this.state.tickets];
                                let aProduits = [...this.state.myProduits];

                                array.push(item);
                                aProduits.push(p);

                                this.setState({
                                    tickets: array,
                                    first: '0',
                                    myProduits: aProduits,
                                    total: parseFloat(first) + this.state.total
                                });
                            }
                        }
                        this.setState({produits: arr, showErrMontant: false, showErrTotal: false});
                    }
                    else {
                        this.setState({showErrMontant: true});
                    }
                }
                else {
                    this.setState({showErrTotal: true});
                }

                break;
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
        let itemSelected = false;
        let arr = [...this.state.produits];
        for (let i = 0; i < this.state.produits.length; i++) {
            if (this.state.produits[i].selected === true) {
                itemSelected = true;
            }
        }

        if(itemSelected === true) {
            const {isResult} = this.state;
            let {first, second, operator} = this.state;
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

                        this.setState({first, second, operator});
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

                        this.setState({first, second, operator});
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

                    this.setState({first, second, operator});

                    break;
                case '+':
                case '−':
                case '×':
                case '÷':
                    if (!operator) {
                        operator = button;

                        this.setState({first, second, operator});
                    } else {
                        this.getResult();
                    }
                    break;
                case 'C':
                    this.setState({showErrMontant: false});
                    this.refresh();
                    break;
                case 'Valider':
                    this.setState({showErrMontant: false});
                    this.getResult(button);
                    break;
                default:
                // console.log('wrong operator');
            }
        }
    }

    _selectItem(item, status) {
        var newItems = [...this.state.produits];
        for (let i = 0; i < this.state.produits.length; i++) {
            if (newItems[i].id === item.id) {
                newItems[i].selected = status;
            } else {
                newItems[i].selected = !status;

            }
        }
        this.setState({ produits : newItems});
    }

    _renderItem = ({item, index}) => {
        if(item.used === true){
            return (
                <View style={styles.GridView} key={index}>
                    <View style={styles.inputGridUsed}>
                        <Text style={styles.textInputSelected}>{item.libelle}</Text>
                        <Text style={styles.textInputSelected}>{item.code}</Text>
                    </View>
                </View>
            )
        }
        else if(!item.selected){
            return(
                <View style={styles.GridView} key={index}>
                    <TouchableOpacity style={[styles.inputGrid, {backgroundColor: '#43a047'}]} onPress={() => {
                        this._selectItem(item, true)}}>
                        <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                            <Text style={[styles.textInput, {color: '#fff'}]}>{item.libelle}</Text>
                            <Text style={[styles.textInput, {color: '#fff'}]}>{item.code}</Text>
                        </View>
                    </TouchableOpacity>
                </View>
            )
        }
        else
            return (
                <View style={styles.GridView} key={index}>
                    <View style={styles.inputGridSelected}>
                        <Text style={styles.textInputSelected}>{item.libelle}</Text>
                        <Text style={styles.textInputSelected}>{item.code}</Text>
                    </View>
                </View>
            )
    };

    _removeItem(index, item){
        let array = [...this.state.tickets];
        let arrP = [...this.state.myProduits];
        array.splice(index,1);
        arrP.splice(index,1);
        let arr = [...this.state.produits];
        for (let i = 0; i < this.state.produits.length; i++) {
            console.log(item);
            console.log(this.state.produits[i]);
            if (this.state.produits[i].libelle === item.nom) {
                arr[i].used = false;
                arr[i].selected = false;
            }
        }
        this.setState({tickets: array, total: this.state.total-item.montant, produits: arr, showErrTotal: false, showSoldeErr: false,  myProduits: arrP});
    }

    _showModal(){
      /*  if(this.state.item.km) {
            this.refs.modalKm.open();
        }
        else {
            this.refs.modalAsk.open();
        }*/

      if(this.state.item.solde < this.state.total) {
          this.setState({showSoldeErr: true})
      }
        else {
          this.setState({showSoldeErr: false})
       /*   this.refs.modalAsk.open();*/
      }

    }

    _goToHome () {
        activityStarter.showPinPadText('');
       /* this.refs.modalSuccess.close();
        this.refs.modalFail.close();*/
        this.props.navigation.navigate('Home');
    }

    _openModal(){
        this.setState({modalLoad: true});
    }

    _checkPincode (pin){
        if(pin !== null){
            if(pin.trim() === "1234"){
                activityStarter.showPinPadText('Pin code correct');
                this._launchPay()
            } else {
                this._pinCodeErr();
            }
        } else {
            this.setState({modalLoad: false});
            ToastAndroid.show('Pin Pad non connecter', ToastAndroid.LONG);
        }
    }

    _pinCodeErr() {
        let tentative = this.state.tentativePin;
        activityStarter.showPinPadText('Pin code incorrect');

        if(tentative <2){
            this.setState({modalLoad: false, modalAsk: true, tentativePin: tentative+1});
        }
        else {
            fetch(host + "/cartes/status/ERRPIN/"+this.state.item.id, {
                method: "PUT",
                headers: {
                    Accept: "application/json",
                    "Content-Type": "application/json",
                    Authorization: "Bearer " + this.props.token
                }
            })
                .then((res) => {
                    this.setState({modalLoad: false, modalAsk: false, modalPinErr: true});
                })
                .catch((err) => {
                    this.setState({modalLoad: false, modalAsk: false, modalFail: true});
                });
        }
    }

    _launchPay() {
        let type= 102;
        let km =0;
        if(this.state.item.typePayement === "PME"){
            type = 105
        }
        if(this.state.inputKm !== ""){
            km = this.state.inputKm;
        }

        this.setState({showPinErr: false, loadingPay:true});
        let ticket = JSON.stringify({
            carte: {id: this.state.item.id},
            station: {id: "5df8f9bb261cf3202ab9a13e"},
            transactions: this.state.myProduits,
            typeTicket: "DEBIT"
        });

        fetch(host + "/tickets/", {
            method: "POST",
            headers: {
                Accept: "application/json",
                "Content-Type": "application/json",
                Authorization: "Bearer " + this.props.token
            },
            body: ticket
        }).then((response) => {
            if (response.status === 200) {
                let carte = this.state.item;
                carte.solde = parseFloat(carte.solde) - parseFloat(this.state.total);
                fetch(host + "/cartes/"+this.state.item.id, {
                    method: "PUT",
                    headers: {
                        Accept: "application/json",
                        "Content-Type": "application/json",
                        Authorization: "Bearer " + this.props.token
                    },
                    body: JSON.stringify(carte)
                }).then((res) => {
                    if (res.status === 200) {
                        response.json().then(data => {
                            const action = { type : "ADD_TICKET", value : data.id}; //changer pour mettre le tableau de transaction
                            this.props.dispatch(action);
                        });
                        this.setState({loadingPay: false, modalAsk:false, modalSuccess:true});
                        activityStarter.showPinPadText('Paiement effectue')
                    }
                    else {
                      activityStarter.showPinPadText('Paiement abandonne');
                      this.setState({loadingPay: false, modalAsk:false, modalFail:true});
                    }
                }).catch((err) => {
                    this.setState({loadingPay: false, modalAsk:false, modalFail:true});
                })
            } else {
                this.setState({loadingPay:false});
                this.setState({loadingPay: false, modalAsk:false, modalFail:true});
            }
        })
            .catch(error => {
                this.setState({loadingPay:false});
                this.setState({loadingPay: false, modalAsk:false, modalFail:true});
            });
    }

    _checkKm(){
        if(this.state.item.solde > this.state.total) {
            if (this.state.item.vehicule.saisi_km) {
                this.setState({modalKm: true})
            } else if (this.state.item.vehicule.code_chauffeur) {
                this.setState({modalCodeChauff: true})
            } else {
                this.setState({modalLoad: true});
            }
        }
        else{
            this.setState({showSoldeErr: true})
        }
    }

    _checkInputKm(km){
        if(km !== null){
            activityStarter.showPinPadText('Kilometrage saisie');
            if(km.trim() > this.state.item.vehicule.km){
              //update le champs km dans vehicule
            } else {
                //demande comfirmation si incohérence
            }
        } else {
            this.setState({modalKm: false});
            ToastAndroid.show('Pin Pad non connecter', ToastAndroid.LONG);
        }
    }

    render() {
        const { loadingPay, loadPay, first, second, operator, result, tickets, total, tentativePin, produits, showError, showErrMontant, showErrTotal, showPinErr, showSoldeErr } = this.state;
        let btn = "Valider";
        return(
            <View style={{flex:1, backgroundColor:'#fafafa'}}>
               <Modal transparent={true} isVisible={this.state.modalKm} onModalShow={() => { activityStarter.Kilometrage((value) => this._checkInputKm(value)) }}>
                   <View style={{flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center'}}>
                       <View style={{width: 370, height: 300, backgroundColor:"#fff", borderRadius:5}}>
                           <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                               <FontAwesome5 name={"spinner"} color={"green"} size={35} style={{marginTop:15}}/>
                               <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Saisie pin code en cours ...</Text>
                           </View>
                       </View>
                   </View>
                </Modal>
                <Modal transparent={true} isVisible={this.state.modalLoad} onModalShow={() => { activityStarter.navigeteMpos((value) => this._checkPincode(value)) }} >
                    <View style={{flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center'}}>
                        <View style={{width: 370, height: 300, backgroundColor:"#fff", borderRadius:5}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"spinner"} color={"green"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Saisie pin code en cours ...</Text>
                            </View>
                        </View>
                    </View>
                </Modal>
                <Modal transparent={true} isVisible={this.state.modalAsk}>
                    <View style={{flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center'}}>
                        <View style={{width: 400, height: 300, backgroundColor:"#fff", borderRadius:5}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center', marginTop:10}}>
                                <FontAwesome5  name={"exclamation-circle"} color={"#e53935"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Erreur Pin Code</Text>
                            </View>
                            <View style={{flex:2, justifyContent:'center', alignItems:'center', marginTop:20}}>
                                <View style={{flex:1, marginTop:15 }}>
                                    {tentativePin === 1 &&
                                    <Text style={{
                                        fontFamily: 'Livvic-Regular',
                                        color: '#757575',
                                        fontSize: 17,
                                        textAlign: 'center'
                                    }}>

                                        Il vous reste 2 tentative
                                    </Text>
                                    }
                                    {tentativePin === 2 &&
                                    <Text style={{
                                        fontFamily: 'Livvic-Regular',
                                        color: '#757575',
                                        fontSize: 17,
                                        textAlign: 'center'
                                    }}>

                                     Il vous reste 1 tentative
                                         </Text>
                                    }
                                </View>
                            </View>
                            <View style={{flex:1, flexDirection:'row', justifyContent:'space-around', alignItems:'flex-end', marginBottom:10, marginTop:5}}>
                                <TouchableOpacity style={{width: 160, height: 45, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                                  onPress={ () => { this.setState({modalAsk:false}); this._openModal() }}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Nouvelle tentative</Text>
                                </TouchableOpacity>
                                <TouchableOpacity style={{width: 180, height: 45, marginLeft:20,  backgroundColor:'red', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                                  onPress={() => { this._goToHome()}}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Annuler le paiement</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </View>
                </Modal>
                <Modal transparent={true} isVisible={this.state.modalSuccess}>
                    <View style={{flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center'}}>
                        <View style={{width: 350, height: 300, backgroundColor:"#fff", borderRadius:5}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"check-circle"} color={"green"} size={35} style={{marginTop:15}}/>
                                <Text style={{fontFamily:'Livvic-Medium', color:'#757575', fontSize:22, marginTop:10}}>Paiement Enregistré !</Text>
                            </View>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <TouchableOpacity style={{width: 140, height: 50, marginTop:20, backgroundColor:'green', justifyContent:'center', alignItems:'center', borderRadius:5}}
                                                  onPress={() => {this._goToHome()}}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#fff', fontSize:16}}>Valider</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </View>
                </Modal>
                <Modal transparent={true} isVisible={this.state.modalFail} >
                    <View style={{flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center'}}>
                        <View style={{width: 450, height: 300, backgroundColor:"#fff", borderRadius:5}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"exclamation-circle"} color={"#e53935"} size={35} style={{marginTop:15}}/>
                                <View style={{flex:1}}>
                                    <Text style={{fontFamily:'Livvic-Medium', color:'#e53935', fontSize:20, marginTop:10, textAlign:'center'}}>Attention ! Une erreur est survenue.</Text>
                                </View>
                                <View style={{flex:1}}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#757575', fontSize:20, marginTop:10, textAlign:'center'}}>La tentative de paiement n'a pas été enregistré</Text>
                                </View>
                            </View>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <TouchableOpacity style={{
                                    width: 200,
                                    height: 50,
                                    marginTop: 20,
                                    backgroundColor: '#e53935',
                                    justifyContent: 'center',
                                    alignItems: 'center',
                                    borderRadius: 5
                                }}
                                                  onPress={() => {
                                                      this._goToHome()
                                                  }}>
                                    <Text style={{
                                        fontFamily: 'Livvic-Regular',
                                        color: '#fff',
                                        fontSize: 16
                                    }}>Annuler</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </View>
                </Modal>
                <Modal transparent={true} isVisible={this.state.modalPinErr} ref={"modalPinErr"}>
                    <View style={{flex: 1, flexDirection: 'column', justifyContent: 'center', alignItems: 'center'}}>
                        <View style={{width: 400, height: 300, backgroundColor:"#fff", borderRadius:5}}>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <FontAwesome5 name={"exclamation-circle"} color={"#e53935"} size={35} style={{marginTop:15}}/>
                                <View style={{flex:1}}>
                                    <Text style={{fontFamily:'Livvic-Medium', color:'#e53935', fontSize:20, marginTop:10, textAlign:'center'}}>Erreur Pin Code ! </Text>
                                </View>
                                <View style={{flex:1}}>
                                    <Text style={{fontFamily:'Livvic-Regular', color:'#757575', fontSize:20, marginTop:10, textAlign:'center'}}>La Carte est bloquée</Text>
                                </View>
                            </View>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <TouchableOpacity style={{width: 200, height: 50, marginTop: 20, backgroundColor: '#e53935',
                                    justifyContent: 'center', alignItems: 'center', borderRadius: 5}} onPress={() => {this._goToHome()}}>
                                    <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 16}}>Retour</Text>
                                </TouchableOpacity>
                            </View>
                        </View>
                    </View>
                </Modal>
                <View style={{width:"94%", height:1, backgroundColor: "#bdbdbd", marginLeft:'3%', marginTop:'6%'}}/>
                {loadPay &&
                    <View style={{flex: 1, backgroundColor: '#fafafa', justifyContent:'center', alignItems:'center'}}>
                        <ActivityIndicator color={'blue'} size={"large"}/>
                    </View>
                }
                { !loadPay &&
                    <View style={{flex:4, padding:4, flexDirection:'row', marginTop:10}}>
                        <View style={{flex:1, height:'95%'}}>
                            <View style={{flex:1}}>
                                <Text style={{fontFamily:'Livvic-Medium', fontSize:20}}>1. Sélectionner un
                                    <Text style={{fontFamily:'Livvic-Bold', fontSize:20}}> produit</Text></Text>
                            </View>
                            <View style={{flex:6}}>
                                <FlatList
                                    contentContainerStyle={{flexGrow: 1, justifyContent: 'center'}}
                                    data={produits}
                                    renderItem={this._renderItem}
                                    keyExtractor={this._keyExtractor}
                                    numColumns={2}
                                />
                            </View>
                        </View>
                        <View style={{flex:1, height:'90%', justifyContent:'center'}}>
                           <View style={{flex:1}}>
                                <Text style={{fontFamily:'Livvic-Medium', fontSize:20}}>2. Renseigner le
                                    <Text style={{fontFamily:'Livvic-Bold', fontSize:20}}> montant</Text>
                                </Text>
                            </View>
                            { showErrMontant && <Text style={styles.textError}>Le montant doit être inférieur à 10 000 € ! </Text>}
                            { showErrTotal &&  <Text style={styles.textError}>Le montant total doit être inférieur à 10 000 € ! </Text>}
                            { showSoldeErr &&  <Text style={styles.textError}>Le solde de la carte est insuffisant ! </Text>}
                            <View style={{flex:6}}>
                            <CalculatorResponse
                                first={first}
                                second={second}
                                operator={operator}
                                result={result}
                                refresh={this.refresh}/>
                            <CalculatorButtonsContainer handleButtonPress={this.handleButtonPress}/>
                            <View style={{justifyContent:"center", alignItems:'center'}}>
                            <TouchableOpacity style={{
                                width: 180,
                                height: 50,
                                backgroundColor: '#03498e',
                                justifyContent: 'center',
                                alignItems: 'center',
                                borderRadius: 5,
                                marginTop: 15
                            }}
                                              onPress={() => {
                                                  this.handleButtonPress(btn)
                                              }}>
                                <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 14}}>Valider</Text>
                            </TouchableOpacity>
                            </View>
                            </View>
                        </View>
                        <View style={{flex:1}}>
                            <View style={{flex:2, marginTop:15, justifyContent:'flex-start', alignItems:'center'}}>
                                <Text style={{fontSize: 20, fontFamily:'Livvic-Medium', marginBottom:20}}>TICKET</Text>
                                { !_.isEmpty(tickets) &&
                                tickets.map((i, index) => {
                                    let montant = parseFloat(i.montant).toFixed(2);
                                    return(
                                        <View style={{flex:1,width:"90%", maxHeight:50, flexDirection:'row', justifyContent:'space-between', alignItems:"flex-start"}} key={index}>
                                            <View style={{flex:1}}><Text style={{fontSize: 17, fontFamily:'Livvic-Regular'}}>{i.nom}</Text></View>
                                            <View style={{flex:1, alignItems:'flex-end'}}><Text style={{fontSize: 20, fontFamily:'Livvic-Medium'}}>{montant.replace('.',',')} €</Text></View>
                                            <View style={{flex:1, alignItems:'flex-end'}}><Fontisto name={"trash"} color="#d32f2f" size={22} onPress={() => {this._removeItem(index, i)}}/></View>
                                        </View>
                                    )
                                })
                                }</View>
                            <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <Text style={{fontFamily:'Livvic-Regular', fontSize:22}}>Total : {total.toFixed(2).replace('.',',')} €</Text>

                                {!_.isEmpty(tickets) &&
                                <TouchableOpacity style={{width: 180, height: 50, backgroundColor: 'green', justifyContent: 'center',
                                    alignItems: 'center', borderRadius: 5, marginTop: 15 }} onPress={() => this._checkKm() }>
                                    <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 14}}>Payer</Text>
                                </TouchableOpacity>
                                }
                            </View>
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
        textAlign:'center',
        textAlignVertical:'center',
        flex:1,
        width:120,
        height: 80,
        elevation : 0,
    },
    inputGrid: {
        flex:1,
        width:120, maxHeight:60,
        borderRadius:6,
        alignItems: 'center',
        justifyContent: 'center',
        textAlign:'center',
        textAlignVertical:'center',
        elevation: 2,
        borderColor:'#e0e0e0', borderWidth:1
    },
    inputGridUsed: {
        flex:1,
        width:120, maxHeight:60,
        borderRadius:6,
        backgroundColor: '#e53935',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign:'center',
        textAlignVertical:'center',
        elevation: 2
    },
    inputGridSelected: {
        flex:1,
        width:120, maxHeight:60,
        borderRadius:6,
        backgroundColor: '#03498e',
        alignItems: 'center',
        justifyContent: 'center',
        textAlign:'center',
        textAlignVertical:'center',
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
        textAlign: 'center',
        color: '#e53935',
        fontSize: 16,
        fontFamily:'Livvic-Regular'
    },
});

const mapStateToProps = (state) => {
    return {
        ticket : state.toggleTicket.ticket,
        token : state.toggleLogin.token
    }
};

export default connect(mapStateToProps)(newOrder);
