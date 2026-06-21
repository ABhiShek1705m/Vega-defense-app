import { useContext } from 'react';
import { Route, Redirect } from 'react-router-dom';
import { UserContext } from './UserProvider';

/**
 * PrivateRoute
 *
 * Wraps react-router's <Route> to enforce authentication at the routing level, not just by hiding nav links.
 *
 * Fixes: "Authenticated Pages Accessible without Login/Credentials"
 */

const PrivateRoute = ({ component: Component, roles, ...rest }) => {
  const { user, loading } = useContext(UserContext);
  if (loading) return null; // wait for auth check to complete
  const isLoggedIn = !!(user && user.jwt && user.email && user.role);
  const hasRequiredRole = !roles || (isLoggedIn && roles.includes(user.role));

  return (
    <Route
      {...rest}
      render={(props) => {
        if (!isLoggedIn) {
          return <Redirect to="/Login" />;
        }
        if (!hasRequiredRole) {
          // Logged in but wrong role — send back to home rather than login
          return <Redirect to="/" />;
        }
        return <Component {...props} />;
      }}
    />
  );
};

export default PrivateRoute;