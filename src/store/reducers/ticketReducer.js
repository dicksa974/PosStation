import moment from 'moment';

const initialState = { ticket: null, expiredAt: null };

function toggleTicket( state = initialState, action ) {
    let nextState;
    console.log("mon ticket ", action.value);
    switch (action.type){
        //LOGGED IN
        case 'ADD_TICKET':
            nextState = {
                ...state,
                ticket : action.value,
                expiredAt : moment().add(5, 'hours'),
            };

            console.log(nextState);

            return nextState || state;

        case 'REMOVE_TRANSACTION':
            nextState = {
                ...state,
                ticket : null,
                expiredAt : null
            };

            return nextState || state;
        default :
            return state;

    }
}

export default toggleTicket;
