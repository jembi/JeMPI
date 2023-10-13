/**
 * @jest-environment jsdom
 */

process.env.REACT_APP_ENABLE_SSO = 'true'

// js-dom does not implement fetch
import { enableFetchMocks } from 'jest-fetch-mock'
enableFetchMocks();

import React, { ReactNode } from "react"
import { AuthChecker, AuthProvider, useAuth } from "../../src/hooks/useAuth"
import { RenderResult, act, fireEvent, render, screen, waitFor } from '@testing-library/react'

import { QueryClient, QueryClientProvider } from "@tanstack/react-query"
import { SnackbarProvider } from "notistack"
import ApiClient from "services/ApiClient"
import lodash from "lodash"
import { RouterProvider, createBrowserRouter, useNavigate } from "react-router-dom"
import baseRouter from "router/BaseRouter"
import keycloakInstance from 'services/keycloak';


describe("useAuth", () => {
    const getPrimitiveValues = (obj:any) => {
        return lodash.omit(obj, ["logout", "refetchUser", "setUser", "signInWithKeyCloak"])
    }
    const getMockUserDetails = () => {
        return {
            id: 1,
            email: "auser@email.com",
            username: "auser",
            familyName: "afamilyName",
            givenName: "aGivenName",
            provider:  'keycloak'
        }
    }

    const mockApiClient = (methodsDic:{[methodName:string]:any}) => {
        for (const [methodName, mockMethod] of Object.entries(methodsDic)){
            jest.spyOn(ApiClient, methodName as keyof typeof ApiClient).mockImplementation(mockMethod)
        }   
    }

    const mockKeyClock = (methodsDic:{[methodName:string]:any}) => {
        for (const [methodName, mockMethod] of Object.entries(methodsDic)){
            jest.spyOn<typeof keycloakInstance, any>(keycloakInstance, methodName).mockImplementation(mockMethod)
        }  
    }
    describe("Auth Provider tests", () => {

        const getAuthProviderComponent = (ChildComponent: ReactNode) => {
            const queryClient = new QueryClient({
                defaultOptions: {
                    queries: {}
                }
            })
    
            return (<SnackbarProvider anchorOrigin={{ horizontal: 'right', vertical: 'top' }}>
                <QueryClientProvider client={queryClient}>
                    <AuthProvider>
                        {ChildComponent}
                    </AuthProvider>
                </QueryClientProvider>
            </SnackbarProvider>)
        }
    
        it("Can load children of provider correctly", () => {
            mockApiClient({
                getCurrentUser: getMockUserDetails
            })

            const SampleComponent = () => {
                return <>SampleComponent</>;
            }
            act(() => {
                render(getAuthProviderComponent(<SampleComponent/>))
            })
    
            expect(screen.getByText("SampleComponent")).toBeTruthy()
        })
    
        it("Can access auth context correctly (loading)", () => {
            mockApiClient({
                getCurrentUser: () => null
            })

            let authContext = null
            const SampleComponent = () => {
                authContext = useAuth()
                return <>SampleComponent</>;
            }
    
            act(() => {
                render(getAuthProviderComponent(<SampleComponent/>))
            })
    
            expect(getPrimitiveValues(authContext)).toEqual({
                error:null,
                isAuthenticated: false,
                isLoading: true,
                user: undefined
            })
        })
    
        it("Can access auth context correctly (loaded)", async () => {
            let authContext:any = null
            
            mockApiClient({
                getCurrentUser: getMockUserDetails
            })
            const SampleComponent = () => {
                authContext = useAuth()
                return <>SampleComponent</>;
            }
    
            await act(() => {
                render(getAuthProviderComponent(<SampleComponent/>))
            })
    
            await waitFor(() => {
                expect(getPrimitiveValues(authContext)).toEqual({
                    error:null,
                    isAuthenticated: true,
                    isLoading: false,
                    user: {...getMockUserDetails()}
                })
            })
            
        })
    })

    describe("Auth Checker tests", () => {
        const getAuthCheckerComponent = (ChildComponent: ReactNode=null) => {
            const queryClient = new QueryClient({
                defaultOptions: {
                    queries: {}
                }
            })
    
                
            return (<SnackbarProvider anchorOrigin={{ horizontal: 'right', vertical: 'top' }}>
                <QueryClientProvider client={queryClient}>
                    <AuthProvider>
                        <RouterProvider router={baseRouter} />
                    </AuthProvider>
                </QueryClientProvider>
            </SnackbarProvider>)
        }

        it("Shows loading if use details are still loading", async () => {
            mockApiClient({
                getCurrentUser: getMockUserDetails
            })

            let renderComponent:any = null
            await act(() => {
                renderComponent = render(getAuthCheckerComponent())
            })

            expect(renderComponent.container.querySelector("#user-loading-spinner")).toBeTruthy()
        })

        it("Navigates user to login page if not logged in", async () => {
            mockApiClient({
                getCurrentUser:  () => null
            })


            await act(() => {
               render(getAuthCheckerComponent())
            })

            await waitFor(() => {
                expect(location.pathname).toEqual("/login")
                expect(screen.getByText("Sign-In with KeyCloak")).toBeTruthy()
                
            })

        })
    
        // it("Navigates to home if on login page, and there is a successfully login", async () => {



        //     mockApiClient({
        //         getCurrentUser: () => {console.log(">>>>"); return null},
        //         validateOAuth: () => Promise.resolve(getMockUserDetails())
        //     })

        //     mockKeyClock({
        //         init: (props:any) => {
        //             baseRouter.state.location.hash = "code=200&state=OK&session_state=12345"
        //             //baseRouter.navigate(0)
        //             // baseRouter.navigate( {
        //             //     pathname: "/login",
        //             //     hash: "code=200&state=OK&session_state=12345",
        //             //     search: "test=true"
        //             // })
        //         }
        //     })

        //     // await act(() => {
               
        //     // })
        //     const compo = render(getAuthCheckerComponent())

        //      await waitFor(() => {
        //          expect(location.pathname).toEqual("/login")
        //          expect(screen.getByText("Sign-In with KeyCloak")).toBeTruthy()
        //      })

        //      await act(() => {
        //         fireEvent.click(screen.getByText('Sign-In with KeyCloak'));
        //     })
            
        //     await act(() => {

        //         render(getAuthCheckerComponent())
        //     })

        //      await waitFor(() => {
        //         expect(location.pathname).toEqual("/")
        //         //TODO
        //     })

        // }, 60000)
    
        it("Renders page directly, if user is logged in", async () => {
            mockApiClient({
                getCurrentUser: getMockUserDetails
            })

            await act(() => {
               render(getAuthCheckerComponent())
            })

            await waitFor(() => {
                expect(location.pathname).toEqual("/")
                //TODO
            })
        })
    })
    
})


