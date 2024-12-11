--
-- PostgreSQL database dump
--

-- Dumped from database version 17.0 (Debian 17.0-1.pgdg120+1)
-- Dumped by pg_dump version 17.0 (Debian 17.0-1.pgdg120+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: User; Type: TABLE; Schema: public; Owner: mtcg
--

CREATE TABLE public."User" (
    username character varying(255) NOT NULL,
    password character varying(255) NOT NULL,
    coins integer DEFAULT 20 NOT NULL,
    score integer DEFAULT 100 NOT NULL,
    elo character varying(50) DEFAULT 'Iron'::character varying NOT NULL,
    game_count integer DEFAULT 0 NOT NULL,
    d_id integer,
    u_id bigint NOT NULL
);


ALTER TABLE public."User" OWNER TO mtcg;

--
-- Name: User_u_id_seq; Type: SEQUENCE; Schema: public; Owner: mtcg
--

CREATE SEQUENCE public."User_u_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public."User_u_id_seq" OWNER TO mtcg;

--
-- Name: User_u_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: mtcg
--

ALTER SEQUENCE public."User_u_id_seq" OWNED BY public."User".u_id;


--
-- Name: card_c_id_seq; Type: SEQUENCE; Schema: public; Owner: mtcg
--

CREATE SEQUENCE public.card_c_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.card_c_id_seq OWNER TO mtcg;

--
-- Name: card; Type: TABLE; Schema: public; Owner: mtcg
--

CREATE TABLE public.card (
    c_id bigint DEFAULT nextval('public.card_c_id_seq'::regclass) NOT NULL,
    name character varying(255) NOT NULL,
    damage integer NOT NULL,
    element_type character varying(50) NOT NULL,
    card_type character varying(50) NOT NULL,
    trait character varying(50) NOT NULL,
    u_id integer
);


ALTER TABLE public.card OWNER TO mtcg;

--
-- Name: deck; Type: TABLE; Schema: public; Owner: mtcg
--

CREATE TABLE public.deck (
    d_id integer NOT NULL,
    c1_id integer,
    c2_id integer,
    c3_id integer,
    c4_id integer
);


ALTER TABLE public.deck OWNER TO mtcg;

--
-- Name: package_p_id_seq; Type: SEQUENCE; Schema: public; Owner: mtcg
--

CREATE SEQUENCE public.package_p_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.package_p_id_seq OWNER TO mtcg;

--
-- Name: package; Type: TABLE; Schema: public; Owner: mtcg
--

CREATE TABLE public.package (
    p_id bigint DEFAULT nextval('public.package_p_id_seq'::regclass) NOT NULL,
    price integer DEFAULT 5 NOT NULL,
    c1_id integer NOT NULL,
    c2_id integer NOT NULL,
    c3_id integer NOT NULL,
    c4_id integer NOT NULL,
    c5_id integer NOT NULL
);


ALTER TABLE public.package OWNER TO mtcg;

--
-- Name: package_u_id_seq; Type: SEQUENCE; Schema: public; Owner: mtcg
--

CREATE SEQUENCE public.package_u_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.package_u_id_seq OWNER TO mtcg;

--
-- Name: User u_id; Type: DEFAULT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public."User" ALTER COLUMN u_id SET DEFAULT nextval('public."User_u_id_seq"'::regclass);


--
-- Data for Name: User; Type: TABLE DATA; Schema: public; Owner: mtcg
--

COPY public."User" (username, password, coins, score, elo, game_count, d_id, u_id) FROM stdin;
\.


--
-- Data for Name: card; Type: TABLE DATA; Schema: public; Owner: mtcg
--

COPY public.card (c_id, name, damage, element_type, card_type, trait, u_id) FROM stdin;
\.


--
-- Data for Name: deck; Type: TABLE DATA; Schema: public; Owner: mtcg
--

COPY public.deck (d_id, c1_id, c2_id, c3_id, c4_id) FROM stdin;
\.


--
-- Data for Name: package; Type: TABLE DATA; Schema: public; Owner: mtcg
--

COPY public.package (p_id, price, c1_id, c2_id, c3_id, c4_id, c5_id) FROM stdin;
\.


--
-- Name: User_u_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mtcg
--

SELECT pg_catalog.setval('public."User_u_id_seq"', 12, true);


--
-- Name: card_c_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mtcg
--

SELECT pg_catalog.setval('public.card_c_id_seq', 1, false);


--
-- Name: package_p_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mtcg
--

SELECT pg_catalog.setval('public.package_p_id_seq', 1, false);


--
-- Name: package_u_id_seq; Type: SEQUENCE SET; Schema: public; Owner: mtcg
--

SELECT pg_catalog.setval('public.package_u_id_seq', 1, false);


--
-- Name: User User_pkey; Type: CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public."User"
    ADD CONSTRAINT "User_pkey" PRIMARY KEY (u_id);


--
-- Name: card card_pkey; Type: CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.card
    ADD CONSTRAINT card_pkey PRIMARY KEY (c_id);


--
-- Name: deck deck_pkey; Type: CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT deck_pkey PRIMARY KEY (d_id);


--
-- Name: package package_pkey; Type: CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_pkey PRIMARY KEY (p_id);


--
-- Name: deck deck_c1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT deck_c1_id_fkey FOREIGN KEY (c1_id) REFERENCES public.card(c_id);


--
-- Name: deck deck_c2_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT deck_c2_id_fkey FOREIGN KEY (c2_id) REFERENCES public.card(c_id);


--
-- Name: deck deck_c3_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT deck_c3_id_fkey FOREIGN KEY (c3_id) REFERENCES public.card(c_id);


--
-- Name: deck deck_c4_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.deck
    ADD CONSTRAINT deck_c4_id_fkey FOREIGN KEY (c4_id) REFERENCES public.card(c_id);


--
-- Name: User fk_deck; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public."User"
    ADD CONSTRAINT fk_deck FOREIGN KEY (d_id) REFERENCES public.deck(d_id);


--
-- Name: package package_c1_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_c1_id_fkey FOREIGN KEY (c1_id) REFERENCES public.card(c_id);


--
-- Name: package package_c2_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_c2_id_fkey FOREIGN KEY (c2_id) REFERENCES public.card(c_id);


--
-- Name: package package_c3_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_c3_id_fkey FOREIGN KEY (c3_id) REFERENCES public.card(c_id);


--
-- Name: package package_c4_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_c4_id_fkey FOREIGN KEY (c4_id) REFERENCES public.card(c_id);


--
-- Name: package package_c5_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: mtcg
--

ALTER TABLE ONLY public.package
    ADD CONSTRAINT package_c5_id_fkey FOREIGN KEY (c5_id) REFERENCES public.card(c_id);


--
-- PostgreSQL database dump complete
--

