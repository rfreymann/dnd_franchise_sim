// src/components/LoginForm.jsx
import React, { useState, useContext } from 'react';
import { AuthContext } from '../auth/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import {
  Box,
  Button,
  Input,
  Text,
  Heading,
  VStack,
  Alert,
  AlertIcon,
  useColorModeValue,
  FormControl,
  FormLabel,
  FormErrorMessage
} from '@chakra-ui/react';

export default function LoginForm() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const { login } = useContext(AuthContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      await login(username, password);
      navigate('/dashboard');
    } catch (err) {
      const msg = err.response?.data?.message || 'Login failed';
      setError(msg);
    }
  };

  return (
    <Box minH="100vh" display="flex" alignItems="center" justifyContent="center" bg={useColorModeValue('gray.50', 'gray.800')} p={4}>
      <Box w="full" maxW="md" p={8} bg={useColorModeValue('white', 'gray.700')} borderRadius="lg" boxShadow="lg">
        <Heading as="h2" size="lg" color="blue.500" textAlign="center" mb={6}>Log in to Your Account</Heading>
        <form onSubmit={handleSubmit}>
          <VStack spacing={5}>
            <FormControl isRequired>
              <FormLabel>Username</FormLabel>
              <Input type="text" value={username} onChange={(e) => setUsername(e.target.value)} placeholder="Enter your username" />
            </FormControl>

            <FormControl isRequired>
              <FormLabel>Password</FormLabel>
              <Input type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="Enter your password" />
            </FormControl>

            {error && (
              <Alert status="error" fontSize="sm">
                <AlertIcon />
                {error}
              </Alert>
            )}

            <Button type="submit" colorScheme="blue" width="full">
              Login
            </Button>
          </VStack>
        </form>
        <Text mt={6} textAlign="center" fontSize="sm" color="gray.600">
          Don't have an account?{' '}
          <Link to="/register" style={{ color: '#3182CE' }}>Register</Link>
        </Text>
      </Box>
    </Box>
  );
}
