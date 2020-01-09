import React from "react";
import {ActivityIndicator, Dimensions, Image, StyleSheet, Text, TouchableOpacity, View} from "react-native";
import { Avatar, Icon, Card } from 'react-native-elements';
import Fontisto from "react-native-vector-icons/Fontisto";
import ButtonHeader from "./../components/ButtonHeader";
import { host } from "../utils/constants";
import moment from "moment";

const { width, height } = Dimensions.get('window');

export default class infoUser extends React.Component {

    static navigationOptions = ({ navigation, screensProps }) => ({
        headerLeft: <Icon type={'simple-line-icon'} name={'arrow-left'} color={'black'} onPress={ () => { navigation.goBack() }}/>,
        headerTitle:
            <View style={{flex:1, flexDirection:'row'}}>
                <Image source={require('../../assets/images/iconx96.jpg')} style={{width:55, height:55, borderRadius:8, marginTop:10}} resizeMode={"contain"}/>
                <Text style={{color:'#03498e', fontSize:20, marginLeft:15, fontFamily:'Livvic-Medium', textAlignVertical:'center'}}>INFORMATION CARTE</Text>
            </View>,
        headerRight: <Icon type={'simple-line-icon'} name={'home'} color={'#03498e'} onPress={ () => { navigation.navigate("Home") }} containerStyle={{marginRight:20}} />,
        headerTransparent: true
    });

    constructor(props) {
        super(props);
        this.state = {
            loading: true,
            currentItem: {},
            opposition: false,
            station: {},
            showErr: false
        };
    }

    async componentDidMount() {
        const { navigation } = this.props;
        const i = navigation.getParam('item', {});
        const s = navigation.getParam('station', {});
        console.log(i);
        this._checkRestrictions(i);
        let o = false;
      /*  if(i.dateOpposition.trim().length !== 0){
            o = true;
        }*/
        this.setState({ currentItem: i, loading: false, opposition: o, station: s });
    }

    _renderItem = ({item, index}) => {
        let color = item.color;
            return (
                <View style={{flex: 1}} key={index}>
                    <View style={styles.GridView}>
                        <TouchableOpacity style={[styles.inputGrid, {backgroundColor: color}]}>
                            {color !== '#fff' && <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <Text style={[styles.textInput, {color: '#fff'}]}>{item.label}</Text>
                                <Text style={[styles.textInput, {color: '#fff'}]}>{item.num}</Text>
                            </View>}
                            {color === '#fff' && <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                <Text style={[styles.textInput, {color: '#616161'}]}>{item.label}</Text>
                                <Text style={[styles.textInput, {color: '#616161'}]}>{item.num}</Text>
                            </View>}
                        </TouchableOpacity>
                    </View>
                </View>
            )
    };

    _checkRestrictions(i){
        let date = moment();
        let hour = date.format("HH:mm");
        let authorize = false;
        i.restriction.forEach(element => {
            switch (element.restrictionJour) {
                case "LUNDI_VENDREDI":
                    if(date.day()>= 1 && date.day()<=5){
                        var hDebut = [element.heure_debut.slice(0, 2), ":", element.heure_debut.slice(2)].join('');
                        var hFin = [element.heure_fin.slice(0, 2), ":", element.heure_fin.slice(2)].join('');
                        if(hour>= hDebut && hour<= hFin){
                            authorize = true;
                        }
                    }
                case "SAMEDI":
                    if(date.day() === 6) {
                        var hDebut = [element.heure_debut.slice(0, 2), ":", element.heure_debut.slice(2)].join('');
                        var hFin = [element.heure_fin.slice(0, 2), ":", element.heure_fin.slice(2)].join('');
                        if(hour>= hDebut && hour<= hFin){
                            authorize = true;
                        }
                    }
                case "DIMANCHE":
                    if(date.day() === 7) {
                        var hDebut = [element.heure_debut.slice(0, 2), ":", element.heure_debut.slice(2)].join('');
                        var hFin = [element.heure_fin.slice(0, 2), ":", element.heure_fin.slice(2)].join('');
                        if(hour>= hDebut && hour<= hFin){
                            authorize = true;
                        }
                    }
            }
        });

        if(authorize){
            this.setState({showAuthErr: false});
        }
        else {
            this.setState({showAuthErr: true});
        }
    }

    render() {
        const { currentItem, loading, showAuthErr } = this.state;
        let color = "#4caf50", text="", showErr=false;

        if (currentItem.status !== "ACTIVE" ) {
            color = "#e53935";
        }
        else if(currentItem.solde <=0){
            color = "#e53935";
            showErr = true;
            text = "Cette carte n'est pas approvionnée !"
        }
        else if(showAuthErr){
            color = "#e53935";
            showErr = true;
            text = "Cette carte n'est pas authorisé dans cette période !"
        }
        return (
            <View style={{flex:1, backgroundColor:'#fafafa'}}>
                <View style={{width:"94%", height:1, backgroundColor: "#bdbdbd", marginLeft:'3%', marginTop:'6%'}}/>
                {loading &&
                <View style={{flex: 1, backgroundColor: '#fafafa', justifyContent:'center', alignItems:'center'}}>
                    <ActivityIndicator color={'blue'} size={"large"}/>
                </View>
                }
                {!loading &&
                    <View style={{flex: 1, marginTop: '2%', alignItems: 'center'}}>
                        { showErr &&  <Text style={{textAlign: 'center', color: '#e53935', fontSize: 16, fontFamily:'Livvic-Regular'}}>
                            {text} </Text>}
                        <Card containerStyle={{width: "80%", height: height / 1.5, alignItems: 'center', borderRadius: 6 }}>
                            <View style={{flex: 1, backgroundColor:color, justifyContent:'center', alignItems:'center'}}>
                                <Avatar size="large" rounded overlayContainerStyle={{backgroundColor: '#fff'}}
                                icon={{name: 'person', type: 'fontisto', color:'#9e9e9e'}} activeOpacity={0.7}/>
                                <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#fff'}}>{currentItem.client.entreprise.enseigne}</Text>
                            </View>
                            <View style={{width: "90%", height: 1, backgroundColor: "#000", marginLeft: '2%'}}/>
                            <View style={{flex: 2, width: "100%", flexDirection:'row', justifyContent:'center', alignItems:'center'}}>
                                    <View style={{flex:2}}>
                                        <View style={{flex:1, flexDirection:'row'}}>
                                            <View style={{flex:1, flexDirection:'row'}}>
                                                <View style={{flex:1, borderRightColor:"#e0e0e0", borderBottomColor:"#e0e0e0", borderRightWidth:1, borderBottomWidth:1}}>
                                                    <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                                        <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#757575', marginLeft:15}}>CARTE</Text>
                                                    </View>
                                                    <View style={{flex:1, flexDirection:'row', justifyContent:'center', alignItems:'center'}}>
                                                        <Fontisto name={"credit-card"} style={{marginTop:5}} size={26} color='#03498e'/>
                                                        <Text style={{fontSize: 18, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#03498e', marginLeft:15}}>{currentItem.serialNumber}</Text>
                                                    </View>
                                                </View>
                                                <View style={{flex:1, borderBottomColor:"#e0e0e0", borderBottomWidth:1}}>
                                                    <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                                        <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#757575', marginLeft:15}}>SOLDE</Text>
                                                    </View>
                                                    <View style={{flex:1, flexDirection:'row', justifyContent:'center', alignItems:'center'}}>
                                                        <Fontisto name={"euro"} style={{marginTop:5}} size={26} color='#03498e'/>
                                               {/*         { currentItem.typePayement === "PME" &&  <Text style={{fontSize: 18, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#03498e', marginLeft:15}}>{parseFloat(currentItem.solde).toFixed(2)}</Text>}
                                                        { currentItem.typePayement !== "PME" &&  <Text style={{fontSize: 18, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#03498e', marginLeft:15}}>-</Text>}*/}
                                                    <Text style={{fontSize: 18, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#03498e', marginLeft:15}}>{parseFloat(currentItem.solde).toFixed(2)}</Text>
                                                    </View>
                                                </View>
                                                <View style={{flex:1, borderLeftColor:"#e0e0e0", borderBottomColor:"#e0e0e0", borderLeftWidth:1, borderBottomWidth:1}}>
                                                    <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                                        <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#757575', marginLeft:15}}>STATUT</Text>
                                                    </View>
                                                    <View style={{flex:1, flexDirection:'row', justifyContent:'center', alignItems:'center'}}>
                                                        <Fontisto name={"eye"} style={{marginTop:5}} size={26} color='#03498e'/>
                                                       <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#03498e', marginLeft:15}}>{currentItem.status}</Text>
                                                     </View>
                                                </View>
                                            </View>
                                        </View>
                                        <View style={{flex:1, flexDirection:'row'}}>
                                            <View style={{flex:1, borderRightColor:"#e0e0e0",borderRightWidth:1}}>
                                                <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                                    <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#757575', marginLeft:15}}>VOITURE</Text>
                                                </View>
                                                <View style={{flex:1, flexDirection:'row', justifyContent:'center', alignItems:'center'}}>
                                                    <Fontisto name={"car"} style={{marginTop:5}} size={26} color='#03498e'/>
                                                    <Text style={{fontSize: 18, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#03498e', marginLeft:15}}>{/*{currentItem.matricule}*/}</Text>
                                                </View>
                                            </View>
                                            <View style={{flex:1, borderRightColor:"#e0e0e0",borderRightWidth:1}}>
                                                <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                                    <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#757575', marginLeft:15}}>UTILISATEUR</Text>
                                                </View>
                                                <View style={{flex:1, flexDirection:'row', justifyContent:'center', alignItems:'center'}}>
                                                    <Fontisto name={"person"} style={{marginTop:5}} size={26} color='#03498e'/>
                                                    <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#03498e', marginLeft:15}} adjustsFontSizeToFit numberOfLines={1}>{/*{currentItem.name}*/}</Text>
                                                </View>
                                            </View>
                                            <View style={{flex:1}}>
                                                <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                                    <Text style={{fontSize: 16, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#757575', marginLeft:15}}>TYPE</Text>
                                                </View>
                                                <View style={{flex:1, justifyContent:'center', alignItems:'center'}}>
                                                    <Text style={{fontSize: 12, fontFamily: 'Livvic-Regular', marginTop: 10, color:'#03498e', marginLeft:5}}>{currentItem.typePayement}</Text>
                                                </View>
                                            </View>
                                        </View>
                                    </View>
                        </View>
                        </Card>
                        <View style={{flex:1,width:"100%", justifyContent:'center', alignItems:'flex-end'}}>{!showErr && <ButtonHeader item={currentItem}/> }
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
        elevation: 2,
        borderColor:'#e0e0e0', borderWidth:1
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
});
