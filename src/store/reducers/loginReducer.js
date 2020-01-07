import moment from 'moment';

const initialState = { loggedIn : false, token: null, expiredAt: null };

function toggleLogin( state = initialState, action ) {
    let nextState;
    switch (action.type){

        //LOGGED IN
        case 'LOGGED_IN':
            console.log(action.value);
            nextState = {
                ...state,
                token : action.value,
                expiredAt : moment().add(24, 'hours'),
                loggedIn : true
            };
            return nextState || state;


        case 'GET_LOGGED_IN':
            return state;


        case 'LOGGED_OUT':
            let loggedIn = false;
            let token = null;
            let expiredAt = null;

            nextState = {
                ...state,
                loggedIn : loggedIn,
                token : token,
                expiredAt : expiredAt
            };

            return nextState || state;
        default :
            return state;

    }
}

export default toggleLogin;