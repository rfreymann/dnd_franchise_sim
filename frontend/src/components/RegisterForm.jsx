// src/components/RegisterForm.jsx
import React, { useState, useContext } from 'react';
import { AuthContext } from '../auth/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import {
  Box,
  Button,
  FormControl,
  FormLabel,
  Input,
  Text,
  Heading,
  VStack,
  Alert,
  AlertIcon,
  useToast,
  useColorModeValue
} from '@chakra-ui/react';

import ReCAPTCHA from 'react-google-recaptcha';
const SITE_KEY = process.env.REACT_APP_RECAPTCHA_SITE_KEY;


export default function RegisterForm() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [recaptchaToken, setRecaptchaToken] = useState(null);
  const [error, setError] = useState(null);
  const { register } = useContext(AuthContext);
  const navigate = useNavigate();
  const toast = useToast();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);

    if (!recaptchaToken) {
      toast({
        title: "Bitte reCAPTCHA bestätigen.",
        status: "warning",
        duration: 3000,
        isClosable: true,
      });
      return;
    }

    try {
      await register(username, password, recaptchaToken);
      toast({
        title: "Register succesful.",
        status: "success",
        duration: 3000,
        isClosable: true,
      });
      navigate('/login');
    } catch (err) {
      const msg = err.response?.data?.message || 'Registration failed';
      setError(msg);
    }
  };

  return (
    <Box minH="100vh" display="flex" alignItems="center" justifyContent="center" bg={useColorModeValue('gray.50', 'gray.800')} p={4}>
      <Box w="full" maxW="md" p={8} bg={useColorModeValue('white', 'gray.700')} borderRadius="lg" boxShadow="lg">
        <Heading as="h2" size="lg" color="blue.500" textAlign="center" mb={6}>Create an Account</Heading>
        <form onSubmit={handleSubmit}>
          <VStack spacing={5}>
            <FormControl id="username" isRequired>
              <FormLabel>Username</FormLabel>
              <Input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
              />
            </FormControl>

            <FormControl id="password" isRequired>
              <FormLabel>Password</FormLabel>
              <Input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter a secure password"
              />
            </FormControl>

            {error && (
              <Alert status="error" fontSize="sm">
                <AlertIcon />
                {error}
              </Alert>
            )}

            <ReCAPTCHA
              sitekey={SITE_KEY}
              onChange={(token) => {
                console.log("New token: ", token);
                setRecaptchaToken(token);
              }}
            />


            <Button type="submit" colorScheme="green" width="full">
              Register
            </Button>
          </VStack>
        </form>
        <Text mt={6} textAlign="center" fontSize="sm" color="gray.600">
          Already have an account?{' '}
          <Link to="/login" style={{ color: '#3182CE' }}>Log in</Link>
        </Text>
      </Box>
    </Box>
  );
}
