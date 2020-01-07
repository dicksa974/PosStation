import React from "react";
import {Text, TouchableOpacity, View} from "react-native";
import { withNavigation } from "react-navigation";
import PropTypes from "prop-types";
import moment from "moment";
import _ from "lodash";

class ButtonHeader extends React.Component {

    static propTypes = {
        item : PropTypes.object.isRequired
    };

    constructor(props) {
        super(props);
    }

    _goNext() {
        this.props.navigation.navigate('NewOrder', {item: this.props.item});
    };

    render() {
        const { item } = this.props;

        if (item.status === "ACTIVE") {
            return (
                <TouchableOpacity style={{
                    width: 160, height: 45, backgroundColor: "#43a047",
                    justifyContent: 'center', alignItems: 'center', marginRight: 10, borderRadius: 5}} onPress={() => this._goNext()}>
                    <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 14}}>Suivant</Text>
                </TouchableOpacity>
            );
        }
        else {
            return (
                <TouchableOpacity style={{
                    width: 160, height: 45, backgroundColor: "#e53935",
                    justifyContent: 'center', alignItems: 'center', marginRight: 10, borderRadius: 5}} onPress={ () => { this.props.navigation.goBack() }}>
                    <Text style={{fontFamily: 'Livvic-Regular', color: '#fff', fontSize: 14}}>Retour</Text>
                </TouchableOpacity>
            );
        }
    }
    }

export default withNavigation(ButtonHeader);
