import React, { createContext, useState, useEffect } from 'react';

const UserContext = createContext(null);

const UserProvider = ({children}) => {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const name  = window.localStorage.getItem("name");
        const email = window.localStorage.getItem("email");
        const role  = window.localStorage.getItem("role");
        const jwt   = window.localStorage.getItem("jwt");
        if (email && jwt) {
            setUser({ name, email, role, jwt });
        }
        setLoading(false);
    }, []);

    const setUserInfo = (name, email, role, jwt) => {
        console.log("SetUserInfo called");
        setUser({ name, email, role, jwt });
        window.localStorage.setItem("name", name);
        window.localStorage.setItem("email", email);
        window.localStorage.setItem("role", role);
        window.localStorage.setItem("jwt", jwt);
    };

    const logout = () => {
        setUser(null);
        window.localStorage.removeItem("name");
        window.localStorage.removeItem("email");
        window.localStorage.removeItem("role");
        window.localStorage.removeItem("jwt");
    };

    return (
        <UserContext.Provider value={{ user, setUserInfo, logout, loading }}>
            {children}
        </UserContext.Provider>
    );
};

export { UserProvider, UserContext };