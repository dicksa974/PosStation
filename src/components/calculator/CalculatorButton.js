import React from 'react';
import { Text, StyleSheet, TouchableOpacity } from 'react-native';

class CalculatorButton extends React.Component {
    render() {
        const { operator, handleButtonPress } = this.props;

        return (
            <TouchableOpacity style={styles.container} onPress={() => handleButtonPress(operator)}>
                <Text style={styles.item}>
                    { operator }
                </Text>
            </TouchableOpacity>
        );
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        backgroundColor: '#03498e',
        margin: 0.5,
    },

    item: {
        color: '#fff',
        fontSize: 26,
        fontFamily:'Livvic-Regular'
    },
});

export default CalculatorButton;