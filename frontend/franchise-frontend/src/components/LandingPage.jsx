// src/components/LandingPage.jsx
import React from 'react';
import { Link as RouterLink } from 'react-router-dom';
import {
  Box,
  Heading,
  Text,
  Button,
  Stack,
  useColorModeValue,
  Container
} from '@chakra-ui/react';

export default function LandingPage() {
  return (
    <Box
      minH="100vh"
      display="flex"
      alignItems="center"
      justifyContent="center"
      bg={useColorModeValue('gray.50', 'gray.800')}
      px={6}
    >
      <Container maxW="2xl" centerContent textAlign="center">
        <Heading size="2xl" color="blue.500" mb={6}>
          Willkommen bei DnD Franchise Manager
        </Heading>
        <Text fontSize="lg" mb={8} color={useColorModeValue('gray.700', 'gray.200')}>
          Baue dein Fantasy-Franchise auf, manage Ressourcen, entsende einzigartige Charaktere und schreibe deine eigene Wirtschaftsgeschichte!
        </Text>
        <Stack direction={{ base: 'column', sm: 'row' }} spacing={4} justify="center">
          <Button
            as={RouterLink}
            to="/login"
            colorScheme="blue"
            size="lg"
            px={8}
          >
            Login
          </Button>
          <Button
            as={RouterLink}
            to="/register"
            variant="outline"
            colorScheme="blue"
            size="lg"
            px={8}
          >
            Registrieren
          </Button>
        </Stack>
      </Container>
    </Box>
  );
}
