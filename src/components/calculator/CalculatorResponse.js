import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity } from 'react-native';

class CalculatorResponse extends React.Component {
    render() {
        const { first, second, operator, result, refresh } = this.props;
        const input = `${first} ${operator} ${second}`;

        return (
            <View>
{/*                <View style={styles.resultContainer}>
                    <Text style={styles.result}>
                        { result }
                    </Text>
                </View>*/}

                <View style={styles.inputContainer}>
                    <Text style={styles.input}>
                        { first === '0' && !operator ? 'Tapez le montant' : input }
                    </Text>
                </View>
            </View>
        );
    }
}

const styles = StyleSheet.create({
    resultContainer: {
        alignItems: 'flex-end',
        backgroundColor: 'rgba(255, 255, 255, 0.1)',
        marginTop: 25,
        paddingVertical: 25,
        paddingRight: 10,
        margin: 1,
    },

    result: {
        color: '#000',
        fontSize: 42,
    },

    inputContainer: {
        flexDirection: 'row',
        justifyContent: 'space-between',
        backgroundColor: 'rgba(255, 255, 255, 0)',
        paddingVertical: 10,
        paddingHorizontal: 5,
    },

    delete: {
        color: 'rgba(0, 0, 0, 0.5)',
        fontSize: 23,
        paddingVertical: 5,
        paddingHorizontal: 10,
    },

    input: {
        color: '#03498e',
        fontSize: 23,
        padding: 5,
        fontFamily:'Livvic-SemiBold'
    },
});

export default CalculatorResponse;