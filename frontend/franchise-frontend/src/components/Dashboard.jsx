// src/components/Dashboard.jsx
import React, { useContext, useEffect, useState } from 'react';
import {
  Box,
  Button,
  Heading,
  VStack,
  Text,
  HStack,
  useToast,
  SimpleGrid,
} from '@chakra-ui/react';
import { AuthContext } from '../auth/AuthContext';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const { fetchFranchises, logout } = useContext(AuthContext);
  const [franchises, setFranchises] = useState([]);
  const toast = useToast();
  const navigate = useNavigate();
  const { isAuthenticated } = useContext(AuthContext);
  useEffect(() => {
    if (!isAuthenticated) return;

    const loadFranchises = async () => {
      try {
        const data = await fetchFranchises();
        setFranchises(data);
      } catch (error) {
        toast({
          title: 'Error loading franchises.',
          description: error.message,
          status: 'error',
          duration: 3000,
          isClosable: true,
        });
      }
    };

    loadFranchises();
  }, [isAuthenticated, fetchFranchises, toast]);



  return (
    <Box maxW="6xl" mx="auto" mt={10} p={6}>
      <HStack justifyContent="space-between" mb={6}>
        <Heading>Dashboard</Heading>
        <HStack spacing={4}>
          <Button colorScheme="blue" onClick={() => navigate('/create-franchise')}>
            Create New Franchise
          </Button>
          <Button colorScheme="red" onClick={logout}>
            Logout
          </Button>
        </HStack>
      </HStack>

      {(!franchises || franchises.length === 0) ? (
        <Text>No franchises found.</Text>
      ) : (
        <SimpleGrid columns={{ base: 1, md: 2, lg: 3 }} spacing={6}>
          {franchises.map((franchise) => (

            <Box
              key={franchise.id}
              p={4}
              borderWidth={1}
              borderRadius="lg"
              shadow="md"
              _hover={{ bg: 'gray.50', cursor: 'pointer' }}
              onClick={() => navigate(`/franchise/${franchise.id}`)}
            >

              <Heading size="md">{franchise.name}</Heading>
              <Text>Funds: {franchise.funds}</Text>
              <Text>Property Value: {franchise.propertyValue}</Text>
              <Text>Unskilled Workers: {franchise.unskilledWorkers}</Text>
              <Text>Low-skilled Workers: {franchise.lowskilledWorkers}</Text>
              <Text>High-skilled Workers: {franchise.highskilledWorkers}</Text>
              <Text>Revenue Modifier: {franchise.revenueModifier}</Text>
              <Text>Upkeep Modifier: {franchise.upkeepModifier}</Text>
            </Box>
          ))}
        </SimpleGrid>
      )}
    </Box>
  );
};

export default Dashboard;
